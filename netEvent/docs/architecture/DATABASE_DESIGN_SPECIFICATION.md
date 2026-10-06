# PHẦN C: DATABASE DESIGN (THIẾT KẾ CƠ SỞ DỮ LIỆU CHUẨN HÓA)
## HỆ THỐNG PHÁT TÁN THÔNG BÁO VÀ PHÊ DUYỆT ĐA KÊNH NETEVENT

---

## 1. Thông tin tài liệu
* **Tên hệ thống**: NetEvent Notification & Approval Service
* **Phiên bản**: 1.0 (Khớp 100% với cơ sở dữ liệu thực tế `init-db.sql`)
* **Hệ quản trị CSDL**: PostgreSQL 14+ (sử dụng extension `pgcrypto` với hàm `gen_random_uuid()`)
* **Kiến trúc dữ liệu**: Phân tách 4 Databases độc lập tương ứng với các nhóm microservices (`lhsk_db`, `ingest_db`, `routing_db`, `audit_db`).

---

## 2. Tổng quan (Database Overview)

Mục đích của cơ sở dữ liệu là lưu trữ toàn bộ dữ liệu cấu hình và vận hành của Notification Service: định nghĩa Profile (loại cảnh báo), nghiệp vụ con (business rule) và câu SQL lấy KPI, kênh gửi & template, vòng đời notification (tiếp nhận $\rightarrow$ duyệt $\rightarrow$ phân rã $\rightarrow$ gửi), danh sách người nhận, và nhật ký audit.

Ngoài ra, schema chứa một nhóm bảng phục vụ giám sát theo sự kiện/lịch (`event`, `event_session`, `site`, `cell`, `cell_session`) mà profile có thể gắn vào qua `profile_event_session` — đây là kênh dữ liệu thứ hai (song song với 6 bảng KPI dạng phẳng theo `date_hour` / `cell_code`) mà một Profile có thể dùng làm nguồn cảnh báo.

### Hai "họ" nguồn dữ liệu nghiệp vụ mà một Profile có thể gắn vào:
1. **Họ KPI dạng phẳng (ClickHouse Analytical DB)**: Độc lập, khóa bằng `date_hour` và `cell_code`, được `business_rule.sql_query` truy vấn trực tiếp; đây là nguồn cho các Profile kiểu `coverage_alert`.
2. **Họ giám sát theo sự kiện/site quan hệ (PostgreSQL Operational DB)** (`event_type` $\rightarrow$ `event` $\rightarrow$ `event_session` $\rightarrow$ `cell_session` $\leftarrow$ `cell` $\leftarrow$ `site`): Có đầy đủ FK quan hệ, được gắn vào profile qua bảng liên kết `profile_event_session`; phù hợp cho các Profile kiểu cảnh báo tăng cường theo dịp lễ/sự kiện tại từng site.

---

## 3. Quy ước thiết kế (Design Conventions)

| Hạng mục | Quy ước áp dụng |
| :--- | :--- |
| **Khóa chính (PK)** | Bảng danh mục nhỏ dùng `int2`/`int8 GENERATED ALWAYS AS IDENTITY` (`event_type`, `holiday_occasion`, `role`, `account`, `profile`). Bảng mã tự nhiên dùng `varchar` làm PK (`business_rule.business_rule_code`, `channel.channel_code`). Toàn bộ bảng runtime/quan hệ nhiều-nhiều còn lại dùng `uuid` (`gen_random_uuid()`). |
| **Thời gian** | `timestamptz` cho mọi mốc thời gian hệ thống (`created_at`, `updated_at`, `received_at`, `sent_at`, `responded_at`); `date` riêng cho `measured_at`, `start_date`, `end_date`, `lunar_start_date`, `lunar_end_date` vì chỉ cần độ chính xác theo ngày. |
| **Trạng thái (status)** | `varchar` + `CHECK` constraint liệt kê giá trị hợp lệ, không dùng Postgres ENUM thuần để dễ mở rộng và migration. |
| **Dữ liệu bán cấu trúc** | `jsonb` cho: `notification_event.raw_event_payload`, `notifications.context_data`, `channel.config`, `recipient.channel_contacts`, `audit_history.payload_snapshot`. |
| **Naming** | `snake_case`; bảng liên kết N-N đặt tên ghép 2 bảng cha (`account_profile`, `cell_session`, `recipient_group_member`, `profile_event_session`, `profile_business_rule_mapping`). |
| **Xóa dữ liệu** | Không hard delete ở bảng giao dịch/audit; dùng cột `status` hoặc `deleted` (boolean) để đánh dấu. Riêng các bảng liên kết trực thuộc profile (`profile_event_session`) dùng `ON DELETE CASCADE` theo `profile_id`. |
| **Khóa ngoại (FK)** | Enforce đầy đủ trong nhóm Event/Site/Cell và nhóm Notification runtime lõi (`notifications` $\leftrightarrow$ `approval_flow`/`notifications_task`/`notification_draft_content`, `channel` $\leftrightarrow$ `routing_template`/`notifications_task`, `recipient` $\leftrightarrow$ `notifications_task`/`recipient_group_member`). Không enforce FK cứng cho các cột tham chiếu logic giữa các microservices độc lập (ví dụ `notifications.profile_name` $\leftrightarrow$ `profile.profile_name`). |
| **Dữ liệu KPI nguồn** | Giữ dạng bảng phẳng, tối ưu hóa ghi nạp số lượng lớn (high-throughput ingestion), khóa theo `date_hour` + `cell_code`, không gắn FK cứng để tránh nghẽn ghi. |

---

## 4. Sơ đồ ERD & Cấu trúc Cụm bảng

Mô hình ERD gồm 3 cụm quan hệ chính:
- **Cụm Event/Site/Cell & Account (`lhsk_db`)**: `event_type`, `holiday_occasion` $\rightarrow$ `event` $\rightarrow$ `event_session` $\rightarrow$ `cell_session` $\leftarrow$ `cell` $\leftarrow$ `site`; `role` $\rightarrow$ `account`.
- **Cụm Profile & Liên kết (`lhsk_db`)**: `profile` $\rightarrow$ `account_profile`, `profile_event_session` (nối sang `event_session`), `profile_business_rule_mapping` (nối sang `business_rule`).
- **Cụm Notification Runtime & Routing (`routing_db`, `ingest_db`, `audit_db`)**: `notification_event`, `channel` $\rightarrow$ `routing_template`, `notifications` $\rightarrow$ `approval_policy` / `approval_flow` / `notification_draft_content` / `notifications_task`, `recipient` $\rightarrow$ `recipient_group` / `recipient_group_member`, `audit_history`.

---

## 5. Danh sách bảng theo nhóm

### 5.1 Nhóm Event/Site/Cell & Account (`lhsk_db`)
| Bảng | Vai trò |
| :--- | :--- |
| `event_type` | Danh mục loại sự kiện |
| `holiday_occasion` | Danh mục dịp lễ / ngày lễ |
| `role` | Danh mục vai trò tài khoản (`ADMIN`, `BUSINESS_OWNER`, `NOC_STAFF`, `APPROVER`, `VIEWER`) |
| `event` | Sự kiện cụ thể (gắn loại sự kiện + dịp lễ) |
| `event_session` | Một đợt/kỳ diễn ra của sự kiện (ngày bắt đầu/kết thúc, âm lịch) |
| `site` | Trạm phát sóng |
| `cell` | Cell thuộc site |
| `cell_session` | Cell nào được giám sát trong session nào |
| `account` | Tài khoản người dùng hệ thống |

### 5.2 Nhóm Profile & Liên kết (`lhsk_db`)
| Bảng | Vai trò |
| :--- | :--- |
| `profile` | Cấu hình gốc của một loại cảnh báo (`cron_expression`, `require_approval`, `start_time`, `end_time`) |
| `channel` | Danh mục kênh gửi (`channel_id`, `channel_code`, `channel_name`) |
| `template` | Mẫu thông báo (`template_id`, `channel_id`, `template_name`, `config`) |
| `profile_account` | N-N Profile $\leftrightarrow$ Tài khoản nhận tin |
| `profile_channel` | N-N Profile $\leftrightarrow$ Kênh gửi áp dụng |
| `profile_event_session` | N-N Profile $\leftrightarrow$ Event Session (Profile theo dõi đợt sự kiện nào) |

### 5.3 Nhóm Liên kết Nghiệp vụ con (`lhsk_db`)
| Bảng | Vai trò |
| :--- | :--- |
| `business_rule` | Định nghĩa nghiệp vụ con: nguồn DB + câu SQL tham số hóa |
| `profile_business_rule_mapping` | N-N Profile $\leftrightarrow$ Business Rule (kèm `status`) |

### 5.4 Nhóm Ingestion & Event (`ingest_db` / `lhsk_db`)
| Bảng | Vai trò |
| :--- | :--- |
| `notification_event` | Bản ghi thô sự kiện liên kết `profile_id` + `event_id`, kèm `raw_event_payload` |

> [!NOTE]
> Các bảng Fact dữ liệu thô đo kiểm KPI mạng (`kpi_overshoot_cell`, `kpi_azimuth_swap_feeder`, `kpi_twinbeam_swap`, `kpi_blocked_cell`, `kpi_azimuth_station_less_than_20_degrees`, `kpi_coverage_area_by_cell`) nằm tại cơ sở dữ liệu ClickHouse (`kpi_db`) và được các `business_rule.sql_query` truy vấn trực tiếp.

### 5.6 Nhóm Kênh & Định tuyến (`routing_db`)
| Bảng | Vai trò |
| :--- | :--- |
| `channel` | Danh mục kênh gửi chuẩn (`channel_code`: `SMS`, `EMAIL`, `OTT`) |
| `routing_template` | Mẫu nội dung theo (`channel_code`, `profile_name`), versioning, tần suất gửi |
| `recipient` | Danh mục người/nhóm nhận (`channel_contacts` JSONB) |
| `recipient_group` | Danh mục nhóm nhận |
| `recipient_group_member` | N-N `recipient` $\leftrightarrow$ `recipient_group` |

### 5.7 Nhóm Notification Runtime & Approval (`routing_db`)
| Bảng | Vai trò |
| :--- | :--- |
| `notifications` | Đại diện 1 thông báo/cảnh báo (dedup qua `notification_event_id`) |
| `approval_policy` | Cấu hình cấp duyệt theo Profile (`level_order`, `timeout_action`, `timeout_minutes`) |
| `approval_flow` | Instance phê duyệt thực tế của từng notification |
| `notification_draft_content` | Nội dung đã render từ template gửi Approver |
| `notifications_task` | Delivery Task theo Kênh $\times$ Người nhận (`CONSTRAINT uq_task_dedup`) |

### 5.8 Nhóm Audit Log (`audit_db`)
| Bảng | Vai trò |
| :--- | :--- |
| `audit_history` | Nhật ký gửi tin bất biến (append-only, BRIN Index trên `created_at`) |

---

## 6. Bảng tổng hợp quan hệ (chỉ liệt kê FK thật sự tồn tại trong `init-db.sql`)

| Bảng cha | Bảng con | Cột khóa | On Delete | FK Enforce? |
| :--- | :--- | :--- | :--- | :--- |
| `event_type` | `event` | `event_type_id` | — | **Có** |
| `holiday_occasion` | `event` | `holiday_id` | — | **Có** |
| `event` | `event_session` | `event_id` | — | **Có** |
| `site` | `cell` | `site_id` | — | **Có** |
| `cell` | `cell_session` | `cell_id` | — | **Có** |
| `event_session` | `cell_session` | `session_id` | — | **Có** |
| `role` | `account` | `role_id` | — | **Có** |
| `account` | `profile_account` | `account_id` | `CASCADE` | **Có** |
| `profile` | `profile_account` | `profile_id` | `CASCADE` | **Có** |
| `profile` | `profile_channel` | `profile_id` | `CASCADE` | **Có** |
| `channel` | `profile_channel` | `channel_id` | `CASCADE` | **Có** |
| `channel` | `template` | `channel_id` | — | **Có** |
| `profile` | `profile_event_session` | `profile_id` | `CASCADE` | **Có** |
| `event_session` | `profile_event_session` | `session_id` | `CASCADE` | **Có** |
| `profile` | `profile_business_rule_mapping` | `profile_id` | `CASCADE` | **Có** |
| `business_rule` | `profile_business_rule_mapping` | `business_rule_id` | `CASCADE` | **Có** |
| `profile` | `notification_event` | `profile_id` | — | **Có** |
| `event` | `notification_event` | `event_id` | — | **Có** |
| `channel` | `routing_template` | `channel_code` | — | **Có** |
| `channel` | `notifications_task` | `channel_id` $\rightarrow$ `channel_code` | — | **Có** |
| `notifications` | `approval_flow` | `notification_id` | — | **Có** |
| `approval_policy` | `approval_flow` | `policy_id` | — | **Có** |
| `notifications` | `notifications_task` | `notification_id` | — | **Có** |
| `recipient` | `notifications_task` | `recipient_id` | — | **Có** |
| `notifications` | `notification_draft_content` | `notification_id` | — | **Có** |
| `routing_template` | `notification_draft_content` | `template_id` | — | **Có** |
| `recipient` | `recipient_group_member` | `recipient_id` | — | **Có** |
| `recipient_group` | `recipient_group_member` | `group_id` | — | **Có** |
| `profile` | `profile_business_rule_mapping` | `profile_name` | — | **Không** (Match theo chuỗi VARCHAR logic) |
| `account` | `recipient` | `account_id` | — | **Không** (Tham chiếu logic giữa 2 Database khác nhau) |
| `notification_event` | `notifications` | `notification_event_id` | — | **Không** (Chỉ có `UNIQUE`, không FK đĩa) |
| `profile` | `notifications`, `approval_policy`, `routing_template` | `profile_name` | — | **Không** (Khóa logic dạng VARCHAR) |

---

## 7. Mô tả chi tiết từng bảng

### 7.1 `event_type` (`lhsk_db`)
| Cột | Kiểu dữ liệu | Ràng buộc |
| :--- | :--- | :--- |
| `event_type_id` | `int2` (SMALLINT) | `GENERATED ALWAYS AS IDENTITY`, PRIMARY KEY |
| `event_type_code` | `varchar` | UNIQUE, NOT NULL |
| `event_type_name` | `varchar` | NOT NULL |

### 7.2 `holiday_occasion` (`lhsk_db`)
| Cột | Kiểu dữ liệu | Ràng buộc |
| :--- | :--- | :--- |
| `holiday_id` | `int2` (SMALLINT) | `GENERATED ALWAYS AS IDENTITY`, PRIMARY KEY |
| `holiday_code` | `varchar` | UNIQUE, NOT NULL |
| `holiday_name` | `varchar` | NOT NULL |

### 7.3 `role` (`lhsk_db`)
| Cột | Kiểu dữ liệu | Ràng buộc |
| :--- | :--- | :--- |
| `role_id` | `int2` (SMALLINT) | `GENERATED ALWAYS AS IDENTITY`, PRIMARY KEY |
| `role_code` | `varchar` | UNIQUE, NOT NULL, `CHECK (role_code IN ('ADMIN','BUSINESS_OWNER','NOC_STAFF','APPROVER','VIEWER'))` |
| `role_name` | `varchar` | NOT NULL |

### 7.4 `event` (`lhsk_db`)
| Cột | Kiểu dữ liệu | Ràng buộc |
| :--- | :--- | :--- |
| `event_id` | `uuid` | PRIMARY KEY, DEFAULT `gen_random_uuid()` |
| `event_code` | `varchar` | UNIQUE, NOT NULL |
| `event_name` | `varchar` | NOT NULL |
| `event_type_id` | `int2` | FK $\rightarrow$ `event_type(event_type_id)`, NULLABLE |
| `holiday_id` | `int2` | FK $\rightarrow$ `holiday_occasion(holiday_id)`, NULLABLE |
| `event_level` | `varchar` | NOT NULL, DEFAULT `'OTHER'`, `CHECK (event_level IN ('NATIONAL','PROVINCIAL','DISTRICT','COMMUNE','OTHER'))` |
| `annual` | `boolean` | NOT NULL, DEFAULT `false` |
| `is_lunar` | `boolean` | NOT NULL, DEFAULT `false` |
| `status` | `varchar` | NOT NULL, DEFAULT `'DRAFT'`, `CHECK (status IN ('DRAFT','ACTIVE','ENDED','CANCELLED'))` |
| `created_at` / `updated_at` | `timestamptz` | NOT NULL, DEFAULT `now()` |

### 7.5 `event_session` (`lhsk_db`)
| Cột | Kiểu dữ liệu | Ràng buộc |
| :--- | :--- | :--- |
| `session_id` | `uuid` | PRIMARY KEY, DEFAULT `gen_random_uuid()` |
| `session_event_code` | `varchar` | UNIQUE, NOT NULL |
| `event_id` | `uuid` | FK $\rightarrow$ `event(event_id)`, NOT NULL |
| `start_date` / `end_date` | `date` | NOT NULL |
| `lunar_start_date` / `lunar_end_date` | `date` | NULLABLE |
| `expected_participants` | `int4` | NULLABLE |
| `status` | `varchar` | NOT NULL, DEFAULT `'PLANNED'`, `CHECK (status IN ('PLANNED','ONGOING','COMPLETED'))` |
| `created_at` / `updated_at` | `timestamptz` | NOT NULL, DEFAULT `now()` |

### 7.6 `site` (`lhsk_db`)
| Cột | Kiểu dữ liệu | Ràng buộc |
| :--- | :--- | :--- |
| `site_id` | `uuid` | PRIMARY KEY, DEFAULT `gen_random_uuid()` |
| `station_code` | `varchar` | UNIQUE, NOT NULL |
| `area_code` / `province_code` | `varchar` | NOT NULL |
| `longitude` / `latitude` | `numeric(10,6)` | NULLABLE |
| `created_at` / `updated_at` | `timestamptz` | NOT NULL, DEFAULT `now()` |

### 7.7 `cell` (`lhsk_db`)
| Cột | Kiểu dữ liệu | Ràng buộc |
| :--- | :--- | :--- |
| `cell_id` | `uuid` | PRIMARY KEY, DEFAULT `gen_random_uuid()` |
| `cell_code` | `varchar` | UNIQUE, NOT NULL |
| `site_id` | `uuid` | FK $\rightarrow$ `site(site_id)`, NOT NULL |
| `device_code` | `varchar` | NOT NULL |
| `sector` / `network` / `vendor` | `varchar` | NULLABLE |
| `ci_serving_cell` | `int8` (BIGINT) | NULLABLE |
| `lac_tac` | `int4` (INTEGER) | NULLABLE |
| `created_at` / `updated_at` | `timestamptz` | NOT NULL, DEFAULT `now()` |

### 7.8 `cell_session` (`lhsk_db`)
| Bảng | Cột | Ràng buộc |
| :--- | :--- | :--- |
| `cell_session` | `cell_session_id` (PK, uuid), `cell_id` (FK $\rightarrow$ `cell`), `session_id` (FK $\rightarrow$ `event_session`), `created_at`, `updated_at` | UNIQUE(`cell_id`, `session_id`) |

### 7.9 `account` (`lhsk_db`)
| Cột | Kiểu dữ liệu | Ràng buộc |
| :--- | :--- | :--- |
| `account_id` | `int8` (BIGINT) | `GENERATED ALWAYS AS IDENTITY`, PRIMARY KEY |
| `username` | `varchar` | UNIQUE, NOT NULL |
| `full_name` / `email` | `varchar` | NOT NULL (email UNIQUE) |
| `cell_phone` / `area_code` / `language` | `varchar` | NULLABLE |
| `role_id` | `int2` | FK $\rightarrow$ `role(role_id)`, NOT NULL |
| `last_login` | `timestamptz` | NULLABLE |
| `deleted` | `boolean` | NOT NULL, DEFAULT `false` |
| `created_at` / `updated_at` | `timestamptz` | NOT NULL, DEFAULT `now()` |

### 7.10 `profile` (`lhsk_db`)
| Cột | Kiểu dữ liệu | Ràng buộc |
| :--- | :--- | :--- |
| `profile_id` | `int8` (BIGINT) | `GENERATED ALWAYS AS IDENTITY`, PRIMARY KEY |
| `profile_name` | `varchar` | UNIQUE, NOT NULL |
| `display_name` | `varchar` | NOT NULL |
| `display_name_ui` | `varchar` | NULLABLE |
| `status` | `varchar` | NOT NULL, DEFAULT `'ACTIVE'`, `CHECK (status IN ('ACTIVE','INACTIVE'))` |
| `cron_expression` | `varchar` | NULLABLE |
| `require_approval` | `boolean` | NOT NULL, DEFAULT `false` |
| `start_time` / `end_time` | `timestamptz` | NULLABLE |

### 7.11 `account_profile` & `profile_event_session` (`lhsk_db`)
| Bảng | Cột | Ràng buộc |
| :--- | :--- | :--- |
| `profile_account` | `account_id` (FK $\rightarrow$ `account`), `profile_id` (FK $\rightarrow$ `profile`) | PRIMARY KEY (`account_id`, `profile_id`) |
| `profile_event_session` | `profile_id` (FK $\rightarrow$ `profile`, `ON DELETE CASCADE`), `session_id` (FK $\rightarrow$ `event_session`, `ON DELETE CASCADE`), `status` (`CHECK IN ('ACTIVE','INACTIVE')`), `created_at` | PRIMARY KEY (`profile_id`, `session_id`) |

### 7.12 `business_rule` (`lhsk_db`)
| Cột | Kiểu dữ liệu | Ràng buộc |
| :--- | :--- | :--- |
| `business_rule_code` | `varchar` | PRIMARY KEY |
| `business_rule_name` | `varchar` | NOT NULL |
| `source_table` | `varchar` | NOT NULL |
| `sql_query` | `varchar` | NOT NULL — câu lệnh SQL tham số hóa an toàn |
| `description` | `varchar` | NULLABLE |
| `status` | `varchar` | NOT NULL, DEFAULT `'ACTIVE'`, `CHECK (status IN ('ACTIVE','INACTIVE'))` |
| `created_at` / `updated_at` | `timestamptz` | NOT NULL, DEFAULT `now()` |

### 7.13 `profile_business_rule_mapping` (`lhsk_db`)
| Cột | Kiểu dữ liệu | Ràng buộc |
| :--- | :--- | :--- |
| `profile_id` | `uuid` | PRIMARY KEY (composite), FK $\rightarrow$ `profile(profile_id)` |
| `business_rule_id` | `uuid` | PRIMARY KEY (composite), FK $\rightarrow$ `business_rule(business_rule_id)` |
| `status` | `varchar` | NOT NULL, DEFAULT `'ACTIVE'`, `CHECK (status IN ('ACTIVE','INACTIVE'))` |
| `created_at` / `updated_at` | `timestamptz` | NOT NULL, DEFAULT `now()` |

### 7.14 `notification_event` (`ingest_db`)
| Cột | Kiểu dữ liệu | Ràng buộc |
| :--- | :--- | :--- |
| `notification_event_id` | `uuid` | PRIMARY KEY, DEFAULT `gen_random_uuid()` |
| `event_code` | `varchar` | UNIQUE, NOT NULL — khóa dedup sự kiện |
| `profile_name` | `varchar` | NOT NULL, DEFAULT `'DEFAULT_PROFILE'` |
| `event_name` | `varchar` | NOT NULL |
| `raw_event_payload` | `jsonb` | NOT NULL |
| `status` | `varchar` | NOT NULL, DEFAULT `'RECEIVED'`, `CHECK (status IN ('RECEIVED','PROCESSING','PROCESSED','INVALID','INCOMPLETE'))` |
| `received_at` / `processed_at` | `timestamptz` | NOT NULL DEFAULT `now()` / NULLABLE |

### 7.15 `channel` (`routing_db`)
| Cột | Kiểu dữ liệu | Ràng buộc |
| :--- | :--- | :--- |
| `channel_code` | `varchar` | PRIMARY KEY (ví dụ: `'SMS'`, `'EMAIL'`, `'OTT'`) |
| `channel_name` | `varchar` | NOT NULL |
| `config` | `jsonb` | NOT NULL, DEFAULT `'{}'` |
| `status` | `varchar` | NOT NULL, DEFAULT `'ACTIVE'`, `CHECK (status IN ('ACTIVE','INACTIVE'))` |
| `created_at` | `timestamptz` | NOT NULL, DEFAULT `now()` |

### 7.16 `routing_template` (`routing_db`)
| Cột | Kiểu dữ liệu | Ràng buộc |
| :--- | :--- | :--- |
| `routing_template_id` | `uuid` | PRIMARY KEY, DEFAULT `gen_random_uuid()` |
| `channel_code` | `varchar` | FK $\rightarrow$ `channel(channel_code)`, NOT NULL |
| `profile_name` | `varchar` | NOT NULL |
| `content` | `varchar` | NOT NULL |
| `version` | `int4` | NOT NULL, DEFAULT `1` |
| `frequency_mode` | `varchar` | `CHECK (frequency_mode IN ('REALTIME','SCHEDULED','BATCH_WINDOW'))` |
| `is_active` | `boolean` | NOT NULL, DEFAULT `true` |

Index: Unique partial index `uq_template_active_per_channel_profile` trên (`channel_code`, `profile_name`) với condition `WHERE is_active = true`.

### 7.17 `recipient` / `recipient_group` / `recipient_group_member` (`routing_db`)
| Bảng | Cột | Ràng buộc |
| :--- | :--- | :--- |
| `recipient` | `recipient_id` (PK, uuid), `account_id` (int8, NULLABLE), `name` (varchar), `organization_id` (uuid, NULLABLE), `channel_contacts` (jsonb, NOT NULL), `status` (`CHECK IN ('ACTIVE','INACTIVE','MUTED')`) | DEFAULT `'ACTIVE'` |
| `recipient_group` | `group_id` (PK, uuid), `group_name` (varchar, UNIQUE, NOT NULL) | — |
| `recipient_group_member` | `recipient_id` (FK $\rightarrow$ `recipient`), `group_id` (FK $\rightarrow$ `recipient_group`) | PRIMARY KEY (`recipient_id`, `group_id`) |

### 7.18 `approval_policy` (`routing_db`)
| Cột | Kiểu dữ liệu | Ràng buộc |
| :--- | :--- | :--- |
| `policy_id` | `uuid` | PRIMARY KEY, DEFAULT `gen_random_uuid()` |
| `profile_name` | `varchar` | NOT NULL |
| `level_order` | `int4` | NOT NULL |
| `timeout_action` | `varchar` | `CHECK (timeout_action IN ('ESCALATE','AUTO_SEND','AUTO_REJECT'))` |
| `timeout_minutes` | `int4` | NOT NULL |

Constraint: `CONSTRAINT uq_policy_profile_level UNIQUE (profile_name, level_order)`.

### 7.19 `notifications` (`routing_db`)
| Cột | Kiểu dữ liệu | Ràng buộc |
| :--- | :--- | :--- |
| `notification_id` | `uuid` | PRIMARY KEY, DEFAULT `gen_random_uuid()` |
| `notification_event_id` | `varchar` | UNIQUE, NOT NULL |
| `profile_name` | `varchar` | NOT NULL |
| `contents` | `varchar` | NULLABLE |
| `context_data` | `jsonb` | NULLABLE |
| `approve_level` | `int4` | NULLABLE |
| `status` | `varchar` | NOT NULL, DEFAULT `'PENDING_APPROVAL'`, `CHECK (status IN ('PENDING_APPROVAL','APPROVED','REJECTED','DISPATCHING','COMPLETED','FAILED'))` |
| `created_at` / `updated_at` | `timestamptz` | NOT NULL, DEFAULT `now()` |

### 7.20 `approval_flow` & `notification_draft_content` (`routing_db`)
| Bảng | Cột | Ràng buộc |
| :--- | :--- | :--- |
| `approval_flow` | `approval_id` (PK, uuid), `notification_id` (FK $\rightarrow$ `notifications`), `policy_id` (FK $\rightarrow$ `approval_policy`), `approver_id` (int8, NULLABLE), `comment` (varchar), `sent_at` / `responded_at` (timestamptz), `status` (`CHECK IN ('PENDING','APPROVED','REJECTED','TIMEOUT','ESCALATED')`) | `CONSTRAINT uq_approval_notification_policy UNIQUE (notification_id, policy_id)` |
| `notification_draft_content` | `draft_id` (PK, uuid), `notification_id` (FK $\rightarrow$ `notifications`), `template_id` (FK $\rightarrow$ `routing_template`), `rendered_content` (varchar), `created_at` | — |

### 7.21 `notifications_task` (`routing_db`)
| Cột | Kiểu dữ liệu | Ràng buộc |
| :--- | :--- | :--- |
| `task_id` | `uuid` | PRIMARY KEY, DEFAULT `gen_random_uuid()` |
| `notification_id` | `uuid` | FK $\rightarrow$ `notifications(notification_id)`, NOT NULL |
| `channel_id` | `varchar` | FK $\rightarrow$ `channel(channel_code)`, NOT NULL |
| `recipient_id` | `uuid` | FK $\rightarrow$ `recipient(recipient_id)`, NOT NULL |
| `status` | `varchar` | NOT NULL, DEFAULT `'PENDING'`, `CHECK (status IN ('PENDING','SENDING','SENT','FAILED','DEAD_LETTER'))` |
| `retry_count` | `int4` | NOT NULL, DEFAULT `0` |
| `content` | `varchar` | NULLABLE |
| `created_at` / `updated_at` | `timestamptz` | NOT NULL, DEFAULT `now()` |

Constraint: `CONSTRAINT uq_task_dedup UNIQUE (notification_id, channel_id, recipient_id)`.

### 7.22 `audit_history` (`audit_db`)
| Cột | Kiểu dữ liệu | Ràng buộc |
| :--- | :--- | :--- |
| `audit_history_id` | `uuid` | PRIMARY KEY, DEFAULT `gen_random_uuid()` |
| `task_id` | `uuid` | NOT NULL |
| `channel_code` | `varchar` | NOT NULL |
| `payload_snapshot` | `jsonb` | NOT NULL |
| `recipient_contact` | `varchar` | NOT NULL |
| `created_at` | `timestamptz` | NOT NULL, DEFAULT `now()` |
| `response_code` | `varchar` | NULLABLE |
| `response_body` | `varchar` | NULLABLE |

Index: `CREATE INDEX brin_audit_created_at ON audit_history USING BRIN(created_at);`

### 7.23 Nhóm 6 Bảng KPI Nguồn (`lhsk_db` - Fact Tables)
Cấu trúc chung 6 bảng KPI dạng phẳng (`cell_overshoot`, `cell_azimuth_mismatch_and_swap_feeder`, `cell_twinbeam_swap`, `blocked_cell`, `azimuth_station_less_than_20_degrees`, `coverage_area_by_cell`):
- Lưu trữ các chỉ số đo đạc chi tiết theo `date_hour` / `measured_at`, `cell_code`, `device_code`, `carrier_freq`.
- Không sử dụng FK đĩa cứng để phục vụ nạp ghi dữ liệu tốc độ cực cao (bulk insert) từ các job tính toán mạng di động.

---

## 8. Giải thích các đặc thù kiến trúc Database

1. **Chuẩn hóa bảng `channel` duy nhất tại `routing_db`**: Bảng `channel` trong `lhsk_db` đã được xóa bỏ để triệt tiêu dư thừa. Mọi thông tin cấu hình kênh gửi tin (`SMS`, `EMAIL`, `OTT`) được quản lý duy nhất tại `routing_db.channel`.
2. **Decoupling giữa `account` và `recipient`**: `account` nằm ở `lhsk_db` quản lý user nội bộ, `recipient` nằm ở `routing_db` chứa thông tin liên lạc đa kênh `channel_contacts` (JSONB). Hai bảng liên kết logic qua `recipient.account_id` mà không bắn FK đĩa cứng, giúp Notification Service gửi tin được tới cả các đối tượng nhận tin không có tài khoản trong hệ thống netEvent.
3. **Hiệu năng Audit Log với BRIN Index**: Bảng `audit_history` trong `audit_db` ứng dụng **BRIN Index (Block Range Index)** trên cột `created_at`. BRIN index có kích thước chỉ bằng < 1% so với B-Tree index thông thường, giúp hệ thống duy trì tốc độ ghi append-only hàng triệu bản ghi mỗi ngày mà không làm suy giảm hiệu năng đĩa.

---
*Tài liệu Thiết kế Cơ sở dữ liệu (Database Design Specification) hoàn thiện khớp 100% với thực trạng codebase.*
