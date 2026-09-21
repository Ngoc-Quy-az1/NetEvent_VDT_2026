+# NetEvent Notification Service
+
+NetEvent nhận sự kiện KPI, xử lý nội dung thông báo, duyệt khi cần thiết, phát tán đa kênh và lưu audit delivery. Các service giao tiếp bất đồng bộ qua Kafka.
+
+## Luồng chạy
+
+`notification-adapter` lưu event và phát `network.report.created`. `event-processor` tạo nội dung; event quan trọng đi qua `approval-service`, các event còn lại chuyển thẳng sang Dispatcher. `dispatcher-service` tách delivery task theo channel. Worker Email, SMS và OTT thực hiện gửi rồi phát `notification.audit.events`; `audit-service` lưu kết quả.
+
+## Chạy local
+
+Yêu cầu Java 11, Maven và Docker Desktop đang chạy.
+
+```powershell
+mvn test
+mvn package -DskipTests
docker compose up --build
+```
+
+Gửi sự kiện thử nghiệm:
+
+```powershell
+Invoke-RestMethod -Method Post -Uri http://localhost:8081/api/v1/events/ingest `
+  -ContentType 'application/json' `
+  -Body '{"eventCode":"KPI-20260914-001","profileName":"COVERAGE_ALERT","type":"KPI_ALERT","payload":{"severity":"WARNING","message":"Coverage threshold exceeded"}}'
+```
+
+Sự kiện `payload.severity = CRITICAL` đi vào approval flow. Lấy approval ID từ topic `notification.approval.requested`, rồi gọi `POST /api/v1/approvals/{approvalId}/decision` với `{"decision":"APPROVED","decidedBy":"operator"}`.
+
+Các API audit hiện có:
+
+- `GET /api/v1/audit/tasks/{taskId}`
+- `GET /api/v1/audit/channels/{channelCode}`
+
+## Kiểm thử
+
+`mvn test -q` kiểm tra Maven reactor và các tình huống ingest mới/trùng lặp. `docker compose config --quiet` kiểm tra cấu hình triển khai.
