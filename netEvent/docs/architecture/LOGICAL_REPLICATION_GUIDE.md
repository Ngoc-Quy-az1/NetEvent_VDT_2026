# BÁO CÁO THỰC HIỆN & HƯỚNG DẪN CẤU HÌNH LOGICAL REPLICATION

Tài liệu này ghi lại chi tiết các bước đã thực hiện để chuẩn hóa CSDL và thiết lập **PostgreSQL Logical Replication** tự động đồng bộ dữ liệu giữa 2 CSDL `adapter_db` (Publisher) và `notification_db` (Subscriber) cho hệ thống **NetEvent**.

---

## 1. MỤC TIÊU VÀ TỔNG QUAN GIẢI PHÁP

### 1.1 Mục tiêu
* **Đồng bộ tự động dữ liệu cấu hình**: Khi có thao tác `INSERT`, `UPDATE`, `DELETE` trên bảng `business_rule` và `profile_business_rule_mapping` ở `adapter_db`, dữ liệu sẽ lập tức được truyền và cập nhật tự động sang `notification_db`.
* **Loại bỏ việc gửi dữ liệu cấu hình qua Kafka**: Giúp Kafka chỉ tập trung truyền tải các event nghiệp vụ thô (`eventId`, `profileId`), tránh việc đẩy dữ liệu nhạy cảm hoặc câu SQL lớn vào Message Queue.
* **Chuẩn hóa Schema theo ERD**: Xóa bỏ các bảng KPI phẳng giả lập không có khóa ngoại khỏi PostgreSQL lõi và chuyển sang định nghĩa tại ClickHouse (`kpi_db`).

### 1.2 Sơ đồ luồng dữ liệu (Logical Replication Flow)

```
               QUẢN LÝ CẤU HÌNH
                     │
                     ▼
┌────────────────────────────────────────┐
│ PostgreSQL Container: netevent-postgres│
│                                        │
│  [Database 1: adapter_db] (Publisher)  │
│  ├── business_rule                     │
│  └── profile_business_rule_mapping     │
│        │                               │
│        │ CREATE PUBLICATION            │
│        │ adapter_config_pub            │
│        │                               │
│        │ WAL Stream (Logical Decoding) │
│        ▼                               │
│  [Database 2: notification_db]         │
│  ├── business_rule (Replica)           │
│  └── profile_business_rule_mapping     │
│        │                               │
│        │ CREATE SUBSCRIPTION           │
│        │ notification_config_sub       │
└────────────────┬───────────────────────┘
                 │
                 ▼
        Processor / Routing Services
       (Đọc dữ liệu cấu hình mới nhất)
```

---

## 2. CHI TIẾT CÁC BƯỚC ĐÃ THỰC HIỆN

### Bước 1: Cấu hình `wal_level=logical` trong `docker-compose.yml`
Để PostgreSQL kích hoạt tính năng phát luồng dữ liệu theo dạng Logical Replication:

* **File đã chỉnh sửa**: [`docker-compose.yml`](file:///d:/netEvent/netEvent/docker-compose.yml)
* **Cấu hình**:
  ```yaml
  services:
    postgres:
      image: postgres:15-alpine
      container_name: netevent-postgres
      environment:
        POSTGRES_USER: postgres
        POSTGRES_PASSWORD: postgrespassword
        POSTGRES_MULTIPLE_DATABASES: adapter_db,notification_db
      ports:
        - "15432:5432"
      command: ["postgres", "-c", "wal_level=logical"]  # <-- Kích hoạt logical replication WAL
      volumes:
        - postgres_data:/var/lib/postgresql/data
        - ./init-db.sql:/docker-entrypoint-initdb.d/init-db.sql
  ```

---

### Bước 2: Thiết lập Publication & Slot bên `adapter_db` (Publisher)
Tại cơ sở dữ liệu nguồn `adapter_db`, đã khai báo công bố dữ liệu (Publication) và tạo sẵn Replication Slot để sẵn sàng phục vụ Subscriber.

* **File đã chỉnh sửa**: [`init-db.sql`](file:///d:/netEvent/netEvent/init-db.sql)
* **Câu lệnh SQL**:
  ```sql
  \connect adapter_db;

  -- 1. Định nghĩa Publication cho 2 bảng cấu hình
  CREATE PUBLICATION adapter_config_pub FOR TABLE 
      business_rule, 
      profile_business_rule_mapping;

  -- 2. Tạo sẵn Logical Replication Slot
  SELECT pg_create_logical_replication_slot('notification_config_sub', 'pgoutput');
  ```

---

### Bước 3: Thiết lập Subscription & Schema bản sao bên `notification_db` (Subscriber)
Vì Logical Replication không tự tạo bảng schema DDL cho bên nhận, tại `notification_db` đã được khởi tạo cấu trúc bảng bản sao và kết nối tới Publication của `adapter_db`.

* **File đã chỉnh sửa**: [`init-db.sql`](file:///d:/netEvent/netEvent/init-db.sql)
* **Câu lệnh SQL**:
  ```sql
  \connect notification_db;

  -- 1. Tạo bảng bản sao business_rule bên notification_db
  CREATE TABLE IF NOT EXISTS business_rule (
      business_rule_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
      business_rule_code VARCHAR NOT NULL UNIQUE,
      business_rule_name VARCHAR NOT NULL,
      source_db_type VARCHAR,
      source_host VARCHAR,
      source_port INT,
      source_database VARCHAR,
      source_schema VARCHAR,
      source_table VARCHAR,
      source_connection_ref VARCHAR,
      sql_query TEXT NOT NULL,
      description VARCHAR,
      status VARCHAR NOT NULL DEFAULT 'ACTIVE',
      max_execution_ms INTEGER DEFAULT 5000,
      allowed_tables VARCHAR[],
      approved_by BIGINT,
      approved_at TIMESTAMPTZ,
      created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
      updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
  );

  -- 2. Tạo bảng bản sao profile_business_rule_mapping bên notification_db
  CREATE TABLE IF NOT EXISTS profile_business_rule_mapping (
      profile_id BIGINT NOT NULL,
      business_rule_id BIGINT NOT NULL,
      execution_order SMALLINT NOT NULL DEFAULT 0,
      sql_query TEXT,
      description VARCHAR,
      status VARCHAR NOT NULL DEFAULT 'ACTIVE',
      created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
      updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
      PRIMARY KEY (profile_id, business_rule_id)
  );

  -- 3. Tạo Subscription kết nối nhận dữ liệu từ adapter_db
  CREATE SUBSCRIPTION notification_config_sub
  CONNECTION 'host=/var/run/postgresql port=5432 dbname=adapter_db user=postgres password=postgrespassword'
  PUBLICATION adapter_config_pub
  WITH (create_slot = false);
  ```

---

### Bước 4: Chuẩn hóa CSDL & Loại bỏ dữ liệu thừa
1. **Loại bỏ các bảng KPI phẳng thô khỏi PostgreSQL**: Xóa các bảng `cell_overshoot`, `blocked_cell`, `azimuth_station_less_than_20_degrees`, `cell_twinbeam_swap`, `cell_azimuth_mismatch_and_swap_feeder`, `coverage_area_by_cell` và `cell_event` khỏi `init-db.sql`.
2. **Xóa kịch bản `INSERT INTO` seed data**: Làm sạch tệp `init-db.sql` chỉ chứa thuần các câu lệnh định nghĩa CSDL (DDL).
3. **Cập nhật tài liệu thiết kế**: Đồng bộ sơ đồ Mermaid ERD tại [`notification_service_erd.mermaid`](file:///d:/netEvent/netEvent/docs/architecture/notification_service_erd.mermaid) và mô tả chi tiết tại [`DATABASE_DESIGN_SPECIFICATION.md`](file:///d:/netEvent/netEvent/docs/architecture/DATABASE_DESIGN_SPECIFICATION.md).

---

## 3. HƯỚNG DẪN KHỞI CHẠY VÀ KIỂM THỬ

### 3.1 Khởi chạy lại Container CSDL
Để làm sạch Volume cũ và khởi chạy CSDL mới áp dụng cấu hình Logical Replication:

```powershell
# Chạy tại thư mục d:\netEvent\netEvent
docker compose down -v
docker compose up -d
```

### 3.2 Kiểm thử tính năng đồng bộ trên DBeaver
1. Kết nối vào CSDL **`adapter_db`** (`localhost:15432`, user: `postgres`, pass: `postgrespassword`).
2. Thực thi câu lệnh chèn dữ liệu thử nghiệm:
   ```sql
   INSERT INTO business_rule (
       business_rule_code, 
       business_rule_name, 
       source_db_type, 
       sql_query, 
       description, 
       status
   ) VALUES (
       'BR_REPLICATION_DEMO', 
       'Kiểm tra tự động đồng bộ', 
       'CLICKHOUSE', 
       'SELECT 1', 
       'Thử nghiệm đồng bộ Logical Replication', 
       'ACTIVE'
   );
   ```
3. Mở kết nối CSDL **`notification_db`** và truy vấn kiểm tra:
   ```sql
   SELECT * FROM business_rule WHERE business_rule_code = 'BR_REPLICATION_DEMO';
   ```
   **Kết quả**: Bản ghi vừa tạo ở `adapter_db` xuất hiện ngay lập tức bên `notification_db`.
