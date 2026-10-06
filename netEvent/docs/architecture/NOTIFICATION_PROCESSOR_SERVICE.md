# Notification Processor Service

## 1. Mục đích

`notification-processor-service` là service trung tâm của phần notification. Nó nhận sự kiện profile từ Kafka, phát acknowledgement để upstream biết sự kiện đã được nhận, và (sau khi gộp dispatcher) lấy các delivery task đang chờ trong `notification_db` để chuyển cho các worker Email, SMS hoặc OTT.

Service chạy bằng Spring Boot, Java 11, PostgreSQL và Kafka. Điểm khởi động là `NotificationProcessorApplication`; scheduler được bật bằng `@EnableScheduling`.

## 2. Luồng đang chạy trong code

```text
notification-adapter
    │ profile.triggered.v1 (ProfileEvent)
    ▼
notification-processor-service
    ├─ ProfileEventConsumer: kiểm tra idempotency và xử lý transaction
    ├─ tạo notification, routing template và task PENDING
    ├─ lưu processed_event
    ├─ publish profile.processing-result.v1 = PROCESSED (sau commit)
    └─ mỗi 5 giây: MessageDispatcherService lấy notifications_task PENDING
             │
             ├─ delivery.email.v1 ──> email-worker
             ├─ delivery.sms.v1   ──> sms-worker
             └─ delivery.ott.v1   ──> ott-worker
                                      │
                                      └─ audit.lifecycle-event.v1
```

### Tiếp nhận `ProfileEvent`

`ProfileEventConsumer` lắng nghe topic `profile.triggered.v1`, consumer group mặc định là `notification-processor-service-group`.

- Event phải có `correlationId`; thiếu giá trị này sẽ ném lỗi để Kafka không coi record là xử lý thành công.
- `correlationId` là khóa idempotency trong `processed_event`. Event đã xử lý sẽ không tạo thêm notification/task và chỉ được ACK lại.
- Với event mới, processor validate payload, kiểm tra rule, materialize template snapshot, tạo notification và delivery task trong một transaction.
- Chỉ sau khi transaction commit thành công, consumer mới phát `ProfileProcessingResultEvent` có trạng thái `PROCESSED` đến `profile.processing-result.v1`.
- Consumer dùng manual record acknowledgement (`AckMode.RECORD`), `enable.auto.commit=false`, tối đa một record mỗi poll và concurrency mặc định là 3.

Rule gate dùng các mapping ACTIVE của profile. Khi profile có rule ACTIVE, event phải mang `payload.ruleResults` với giá trị boolean `true` theo từng `businessRuleCode`; thiếu hoặc `false` sẽ rollback transaction để Kafka retry. Processor không thực thi trực tiếp trường `business_rule.sql_query`, vì cấu hình hiện không có executor/datasource credential an toàn cho các nguồn dữ liệu động.

## 3. Dispatch delivery (đã gộp từ dispatcher-service)

`MessageDispatcherService` chạy theo `dispatcher.poll-interval-ms` (mặc định 5 giây).

1.fChọn tối đa `dispatcher.batch-size` task `PENDING` (mặc định 50).
2. Chỉ chọn recipient/group có `status = ACTIVE` và đã tới `scheduled_at`.
3. Dùng `FOR UPDATE SKIP LOCKED`, sau đó đổi task sang `SENDING`. Nhiều instance processor có thể chạy song song mà không claim cùng một task.
4. Đọc target contact từ `recipient.channel_config` hoặc `notification_group.group_config`; nội dung được lấy từ `notifications.context_data.renderedMessages.<CHANNEL>.content`.
5. Đọc attachment `READY` từ `notification_attachment`, đóng gói `DeliveryTaskEvent` rồi phát đến topic theo channel.
6. Gửi Kafka thành công thì đổi task thành `SENT` và phát lifecycle event `TaskDispatched` tới `audit.lifecycle-event.v1`.
7. Có lỗi thì tăng `retry_count`; task trở lại `PENDING`, hoặc thành `DEAD_LETTER` khi đạt `dispatcher.max-retries` (mặc định 3).

Mapping channel hiện tại:

- `SMS` → `delivery.sms.v1`.
- `OTT` hoặc `TELEGRAM` → `delivery.ott.v1`.
- Mọi giá trị khác → `delivery.email.v1`.

## 4. Dữ liệu sở hữu/sử dụng

Service kết nối `notification_db`. Schema được khởi tạo bởi `deploy/database/init/init-db.sql`; processor không chạy Flyway (`spring.flyway.enabled=false`). Các bảng quan trọng:

- `notifications`: notification gốc, `context_data` JSONB và trạng thái.
- `notifications_task`: đơn vị delivery; có một recipient hoặc một group và có trạng thái/retry count.
- `recipient`, `notification_group`: target và cấu hình contact theo channel (JSONB).
- `routing_template`: template snapshot materialized từ event để task có khóa ngoại hợp lệ.
- `business_rule`, `profile_business_rule_mapping`: rule gate theo kết quả trong event payload. `business_rule.sql_query` là mảng `TEXT[]` query chi tiết; `summary_sql_queries` là mảng `TEXT[]` query tóm tắt. Hai cột này độc lập, không dùng JSONB.
- `notification_attachment`: file đính kèm; dispatcher chỉ gửi attachment có trạng thái `READY`.
- `processed_event`: mô hình idempotency, nhưng hiện chưa được consumer sử dụng.

## 5. Cấu hình cần biết

- `KAFKA_SERVERS` (dev: `localhost:9092`): Kafka bootstrap server.
- `DB_HOST`, `DB_PORT` (dev: `localhost`, `15432`): PostgreSQL.
- `NOTIFICATION_DB_NAME` (dev: `notification_db`): database notification.
- `PROCESSOR_KAFKA_CONCURRENCY` (mặc định `3`): số consumer Kafka song song.
- `DISPATCHER_POLL_INTERVAL_MS` (mặc định `5000`): chu kỳ quét task chờ.
- `DISPATCHER_BATCH_SIZE` (mặc định `50`): số task claim trong một lượt.
- `DISPATCHER_MAX_RETRIES` (mặc định `3`): số lần publish lỗi trước khi task thành `DEAD_LETTER`.

Production dùng Kafka `kafka:29092` và PostgreSQL `postgres:5432` theo `application-prod.properties`.

## 6. Điểm cần kiểm tra trước khi vận hành

1. DDL đã bổ sung `notifications.updated_at`, `notifications_task.channel_code` và các cột routing template cần thiết. Với database đã tồn tại, chạy một lần `deploy/database/migrations/V2__notification_processor_columns.sql`; docker init script chỉ áp dụng cho database mới.
2. Trước khi deploy model rule mới, chạy `deploy/database/migrations/V3__business_rule_sql_arrays.sql` trên **cả** `adapter_db` và `notification_db`. Migration chuyển giá trị SQL cũ thành mảng một phần tử và thêm `summary_sql_queries` rỗng.
3. Đảm bảo producer tạo `templates`, `accounts` và `profileGroups` theo snapshot Adapter hiện có; processor sẽ chuyển chúng thành `context_data.renderedMessages` và `channel_config`/`group_config`.
4. Với profile có rule ACTIVE, Adapter hoặc bước đánh giá KPI phải gửi ví dụ `"ruleResults": { "RULE_CODE": true }`.
5. Format context/target mà dispatcher dùng có dạng:

```json
{
  "renderedMessages": {
    "EMAIL": { "content": "Nội dung email" }
  }
}
```

```json
{
  "EMAIL": { "enabled": true, "contact": "user@example.com" }
}
```

6. `SENT` hiện nghĩa là processor đã publish event cho worker, không khẳng định provider đã gửi thành công. Kết quả delivery cuối cùng cần lấy từ worker/audit.

## 7. Cấu trúc source

```text
src/main/java/com/example/notification/processor
├── NotificationProcessorApplication.java     # boot + scheduler
├── config/KafkaConsumerConfig.java            # Kafka consumer factory
├── consumer/                                  # nhận ProfileEvent, publish acknowledgement
├── application/dispatch/                      # claim/retry/publish delivery task
├── entity/                                    # JPA mapping notification_db
├── repository/                                # Spring Data repository
└── dto/                                       # model phục vụ processing/rule/template
```

## 8. Kiểm tra build

```powershell
mvn -pl services/notification-processor-service -am test
```
