# Thiết kế tạo và gửi file báo cáo KPI

## 1. Mục tiêu

Tạo một báo cáo (Excel, sau này có thể là PDF/CSV) đúng **một lần** từ kết quả KPI đã chốt; lưu file bền vững; sau đó cho phép Email, OTT hoặc channel mới gửi đúng file đó.

Worker là delivery adapter. Worker không chạy business rule, không query KPI và không tạo lại file.

## 2. Quyết định kiến trúc

Tách khả năng tạo tài liệu thành `report-service` (có thể ban đầu là module trong `notification-processor-service`, rồi tách deploy độc lập khi tải tăng).

`notification-processor-service` gọi report-service ngay sau khi thực thi rules. Chỉ khi file có trạng thái `READY` mới tạo task gửi. File được lưu tại object storage dùng chung, ví dụ MinIO hoặc S3. Kafka và database chỉ mang metadata file, không mang bytes của file.

```text
PROFILE_TRIGGERED
       |
       v
notification-processor
  1. chạy KPI rules và chốt RuleResult
  2. tạo ReportRequest
       |
       v
report-service
  3. dựng XLSX / PDF / CSV
  4. upload Object Storage
  5. cập nhật attachment = READY
       |
       v
notification-processor
  6. tạo Notification + NotificationTask(PENDING)
       |
       v
dispatcher (poll DB)
  7. claim task + lấy attachment READY
  8. publish DeliveryTaskEvent chỉ chứa attachment refs
       |
       v
email/ott/sms worker
  9. tải file qua storageKey hoặc signed URL
 10. gửi file hoặc link theo khả năng channel
```

## 3. Ranh giới trách nhiệm

| Thành phần | Trách nhiệm | Không chịu trách nhiệm |
|---|---|---|
| notification-processor | Thực thi rule, tạo snapshot dữ liệu, yêu cầu report, tạo notification/task | Render hoặc gửi lại file ở thời điểm retry delivery |
| report-service | Validate request, tạo document, upload storage, quản lý vòng đời file | Chọn recipient hoặc gửi notification |
| object storage | Lưu bytes, version, checksum, TTL/lifecycle | Business rule hoặc routing |
| dispatcher | Claim task DB, lấy attachment metadata, phát delivery event | Tạo Excel hoặc tải nội dung file vào Kafka |
| worker | Tải file / signed URL và gửi qua provider | Query KPI, render report, sửa metadata báo cáo |

## 4. Data model

Không dùng `notifications.context_data` làm nguồn attachment chính; JSON này chỉ phù hợp để lưu context phụ. Tạo bảng quan hệ rõ ràng.

```sql
CREATE TABLE notification_attachment (
    attachment_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    notification_id UUID NOT NULL REFERENCES notifications(notification_id) ON DELETE CASCADE,
    report_request_id UUID NULL,
    file_name VARCHAR(512) NOT NULL,
    storage_provider VARCHAR(32) NOT NULL, -- MINIO | S3 | LOCAL (chỉ dev)
    storage_bucket VARCHAR(255) NULL,
    storage_key VARCHAR(1024) NOT NULL,
    content_type VARCHAR(255) NOT NULL,
    size_bytes BIGINT NOT NULL,
    checksum_sha256 VARCHAR(64) NOT NULL,
    status VARCHAR(20) NOT NULL, -- REQUESTED | GENERATING | READY | FAILED | EXPIRED
    error_message TEXT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    ready_at TIMESTAMPTZ NULL,
    expires_at TIMESTAMPTZ NULL
);

CREATE INDEX idx_attachment_notification_ready
    ON notification_attachment(notification_id, status);
```

Tách bảng request nếu report được xử lý bất đồng bộ hoặc cần audit/tái tạo:

```sql
CREATE TABLE report_request (
    report_request_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    event_id UUID NOT NULL,
    profile_id UUID NOT NULL,
    report_type VARCHAR(64) NOT NULL, -- KPI_DETAIL_XLSX, KPI_SUMMARY_XLSX, ...
    source_snapshot JSONB NOT NULL,
    status VARCHAR(20) NOT NULL, -- REQUESTED | GENERATING | READY | FAILED
    idempotency_key VARCHAR(255) NOT NULL UNIQUE,
    error_message TEXT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    completed_at TIMESTAMPTZ NULL
);
```

`source_snapshot` phải là dữ liệu đã chốt từ `RuleResult`, không phải SQL query để worker chạy lại. Với file lớn, snapshot có thể là một object JSON/Parquet trong object storage và bảng chỉ lưu key.

## 5. Contract

### 5.1 ReportRequest

```json
{
  "reportRequestId": "uuid",
  "idempotencyKey": "eventId:ruleCode:KPI_DETAIL_XLSX:v1",
  "eventId": "uuid",
  "profileId": "uuid",
  "reportType": "KPI_DETAIL_XLSX",
  "templateVersion": "v1",
  "fileName": "CoverageOvershoot_20260924.xlsx",
  "snapshot": {
    "ruleCode": "OVERSHOOT",
    "columns": ["cell", "site", "value"],
    "rows": []
  }
}
```

`idempotencyKey` giúp retry request không sinh ra nhiều file trùng cho cùng event/rule/template.

### 5.2 DeliveryTaskEvent

Mở rộng `DeliveryTaskEvent` bằng `attachments`; tuyệt đối không encode base64 file vào message.

```json
{
  "taskId": "uuid",
  "notificationId": "uuid",
  "channel": "EMAIL",
  "recipientTarget": "user@example.com",
  "title": "Cảnh báo chất lượng mạng",
  "body": "Số liệu chi tiết trong file đính kèm.",
  "attachments": [
    {
      "attachmentId": "uuid",
      "fileName": "CoverageOvershoot_20260924.xlsx",
      "storageKey": "reports/2026/09/24/...xlsx",
      "contentType": "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
      "sizeBytes": 12345,
      "checksumSha256": "..."
    }
  ]
}
```

Signed URL chỉ nên được tạo gần thời điểm gửi, có TTL ngắn. Không lưu signed URL vào DB vì URL sẽ hết hạn.

## 6. Luồng xử lý chi tiết

### Phương án A — đồng bộ (khuyến nghị khi file nhỏ và tạo nhanh)

1. Processor chạy toàn bộ rules và nhận `RuleResult`.
2. Processor gọi `report-service` nội bộ/HTTP/gRPC để tạo file.
3. Report-service tạo workbook bằng Apache POI streaming (`SXSSFWorkbook` khi nhiều dòng), upload object storage và trả metadata `READY`.
4. Processor mở transaction DB, lưu `notifications`, `notification_attachment(READY)` và `notifications_task(PENDING)`.
5. Dispatcher poll task `PENDING`, chỉ claim nếu mọi attachment bắt buộc là `READY`.
6. Dispatcher publish delivery event kèm metadata attachment.
7. Worker tải bytes từ storage, kiểm tra checksum, gọi provider và ghi delivery log.

Phương án này phù hợp với đa số report KPI vài MB đến vài chục MB.

### Phương án B — bất đồng bộ (file lớn hoặc render lâu)

1. Processor lưu `report_request(REQUESTED)` và `notification(status=WAITING_REPORT)`.
2. Report-service consume request hoặc poll bảng request, atomically chuyển `GENERATING`.
3. Khi hoàn tất, report-service upload file, tạo attachment `READY`, cập nhật request `READY`.
4. Một orchestrator/job kiểm tra toàn bộ report request của notification đã `READY` rồi tạo task `PENDING`.
5. Dispatcher tiếp tục như phương án A.

Không tạo task gửi trước khi file bắt buộc `READY`; điều này tránh việc dispatcher hoặc worker phải chờ lâu/retry vì file chưa có.

## 7. Lưu trữ file

### Production

- Dùng MinIO/S3, bucket private, encryption at rest nếu dữ liệu KPI nhạy cảm.
- Key nên có tính bất biến và tránh collision:

```text
reports/{profileId}/{eventId}/{ruleCode}/{reportRequestId}/{fileName}
```

- Upload trước, chỉ ghi `READY` sau khi upload thành công và có checksum.
- Dùng lifecycle policy xóa hoặc archive file theo retention, ví dụ 90/180 ngày.
- Service account của worker chỉ có quyền đọc bucket/prefix cần thiết; worker không có quyền ghi.

### Development

`LocalReportStorage` hiện tại ghi vào local path. Nó chỉ dùng được nếu processor và worker dùng chung volume. Trong Docker/Kubernetes nhiều pod, local disk của processor không thể được worker đọc; vì vậy chỉ dùng local storage ở dev/test.

Lưu ý cấu hình hiện tại không thống nhất: Java đọc `app.report.storage.base-path`, trong khi property files dùng `report.storage.base-path`. Chuẩn hóa thành một key trước khi triển khai, ví dụ `app.report.storage.base-path`.

## 8. Trạng thái, retry và idempotency

```text
ReportRequest: REQUESTED -> GENERATING -> READY
                                      -> FAILED

Attachment:    REQUESTED -> READY -> EXPIRED
                            -> FAILED

Task:          PENDING -> SENDING -> SENT
                   ^          |
                   |          v
                   +------ FAILED -> DEAD_LETTER
```

- Retry render phải dùng `idempotencyKey`; không tạo Excel mới nếu file `READY` cùng checksum đã tồn tại.
- Retry delivery tái sử dụng cùng `attachmentId`/`storageKey`, không tạo report mới.
- Kafka publish và cập nhật DB không phải atomic tuyệt đối. Worker cần idempotent theo `taskId`; provider send nên có delivery idempotency key nếu provider hỗ trợ.
- Dispatcher cần recovery cho task bị kẹt `SENDING` (ví dụ process chết) bằng timeout có cấu hình.

## 9. Khác biệt theo channel

| Channel | Cách giao file |
|---|---|
| Email | Worker tải bytes, attach MIME, giới hạn dung lượng cấu hình; vượt ngưỡng thì gửi signed URL |
| OTT/Telegram | Upload document qua API provider nếu hỗ trợ; nếu không gửi signed URL |
| SMS | Không attach file; gửi signed URL rút gọn |
| Channel mới | Chỉ implement adapter nhận attachment metadata; không cần biết KPI/rule/Excel |

## 10. Kế hoạch triển khai

1. Tạo DTO `AttachmentRef` và thêm `List<AttachmentRef> attachments` vào `DeliveryTaskEvent`.
2. Thêm migration `notification_attachment` trong **notification-processor-service** (service sở hữu schema notification).
3. Tách interface `ReportStorage` thành implementation `S3/MinioReportStorage`; giữ `LocalReportStorage` cho profile dev.
4. Chuẩn hóa config storage và thêm bucket/credential/retention configuration.
5. Điều chỉnh `ExcelReportGenerator`: nhận snapshot, dùng `SXSSFWorkbook` cho report lớn, trả checksum/size/storage key.
6. Processor lưu attachment metadata trước khi task được `PENDING`.
7. Dispatcher join attachment `READY`, thêm refs vào `DeliveryTaskEvent`; task có attachment chưa `READY` không được claim.
8. Cập nhật email worker attach file; OTT worker gửi document hoặc signed URL.
9. Thêm integration tests: một event KPI -> file tồn tại object storage -> attachment DB `READY` -> worker nhận attachment reference.
10. Thêm metric: `report_generation_duration`, `report_generation_failed`, `attachment_size_bytes`, `delivery_attachment_failed`.

## 11. Tiêu chí nghiệm thu

- Một event/rule chỉ tạo một file với cùng idempotency key.
- File có checksum, metadata DB và object storage key nhất quán.
- Retry worker không tạo hay thay đổi file.
- Dispatcher không publish task khi attachment bắt buộc chưa `READY`.
- Email/OTT gửi được file hoặc link theo policy size/channel.
- Không có binary attachment trong Kafka, database JSON context hoặc log.
