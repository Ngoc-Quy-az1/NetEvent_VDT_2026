# SQL KPI theo event session của giờ liền trước

Tài liệu này là bộ SQL ClickHouse cho sáu nghiệp vụ KPI. Mỗi câu lệnh cùng áp dụng hai điều kiện bắt buộc:

1. Lấy **trọn giờ liền trước**, từ đầu giờ trước đến trước đầu giờ hiện tại.
2. `event_code` thuộc mảng `payload.eventSessionCodes` của message Kafka `PROFILE_TRIGGERED`.

Ví dụ message:

```json
{
  "payload": {
    "eventSessionCodes": ["LE_2026_01", "LE_2026_02"]
  }
}
```

`event_code` trong các bảng KPI được KPI simulator gán từ `event_session.session_event_code`; do đó hai giá trị trên là cùng một loại mã.

## Quy ước tham số

Các SQL dùng ClickHouse query parameter:

```sql
{eventSessionCodes:Array(String)}
```

Khi gọi ClickHouse HTTP API, truyền parameter này từ mảng `payload.eventSessionCodes`. Ví dụ giá trị tương đương khi chạy thủ công là:

```sql
['LE_2026_01', 'LE_2026_02']
```

Không thay điều kiện thời gian bằng `date_hour >= now() - INTERVAL 1 HOUR`: cách đó là cửa sổ trượt và có thể lẫn dữ liệu của giờ hiện tại. Bộ SQL bên dưới luôn lấy một giờ KPI đã hoàn tất.

## 1. Cell overshoot

### `sql_query` — dữ liệu chi tiết

```sql
SELECT
    date_hour,
    area_code,
    province_code,
    event_type,
    event_code,
    event_name,
    cell_code,
    device_code,
    sector,
    carrier_freq,
    number_overshoot_sample,
    total_mdt_sample,
    overshoot_rate,
    insert_ts
FROM kpi_db.kpi_overshoot_cell
WHERE date_hour >= toStartOfHour(now()) - INTERVAL 1 HOUR
  AND date_hour <  toStartOfHour(now())
  AND event_code IN {eventSessionCodes:Array(String)}
ORDER BY date_hour, province_code, area_code, cell_code;
```

### `summary_sql_queries` — dữ liệu điền template

```sql
SELECT
    count() AS overshoot_total_records,
    uniqExact(cell_code) AS overshoot_affected_cells,
    round(avg(overshoot_rate), 2) AS overshoot_avg_rate,
    round(max(overshoot_rate), 2) AS overshoot_max_rate,
    round(sum(number_overshoot_sample), 2) AS overshoot_total_samples
FROM kpi_db.kpi_overshoot_cell
WHERE date_hour >= toStartOfHour(now()) - INTERVAL 1 HOUR
  AND date_hour <  toStartOfHour(now())
  AND event_code IN {eventSessionCodes:Array(String)};
```

## 2. Sai azimuth và swap feeder

### `sql_query` — dữ liệu chi tiết

```sql
SELECT
    date_hour,
    area_code,
    province_code,
    event_type,
    event_code,
    event_name,
    device_code,
    sector,
    cell_code,
    carrier_freq,
    number_ue_have_mdt,
    azi_nims,
    azi_predict,
    delta_azi,
    type,
    insert_ts
FROM kpi_db.kpi_azimuth_swap_feeder
WHERE date_hour >= toStartOfHour(now()) - INTERVAL 1 HOUR
  AND date_hour <  toStartOfHour(now())
  AND event_code IN {eventSessionCodes:Array(String)}
ORDER BY date_hour, province_code, area_code, device_code, cell_code;
```

### `summary_sql_queries` — dữ liệu điền template

```sql
SELECT
    count() AS azimuth_swap_total_records,
    uniqExact(cell_code) AS azimuth_swap_affected_cells,
    round(avg(abs(delta_azi)), 2) AS azimuth_swap_avg_delta_azi,
    round(max(abs(delta_azi)), 2) AS azimuth_swap_max_delta_azi,
    round(sum(number_ue_have_mdt), 2) AS azimuth_swap_total_ue_mdt
FROM kpi_db.kpi_azimuth_swap_feeder
WHERE date_hour >= toStartOfHour(now()) - INTERVAL 1 HOUR
  AND date_hour <  toStartOfHour(now())
  AND event_code IN {eventSessionCodes:Array(String)};
```

## 3. Cell twinbeam swap fiber

### `sql_query` — dữ liệu chi tiết

```sql
SELECT
    date_hour,
    area_code,
    province_code,
    event_type,
    event_code,
    event_name,
    device_code,
    sector,
    cell_code,
    cell_twinbeam,
    carrier_freq,
    azi_predict,
    delta_twb,
    cqi,
    ri1_rate,
    insert_ts
FROM kpi_db.kpi_twinbeam_swap_fiber
WHERE date_hour >= toStartOfHour(now()) - INTERVAL 1 HOUR
  AND date_hour <  toStartOfHour(now())
  AND event_code IN {eventSessionCodes:Array(String)}
ORDER BY date_hour, province_code, area_code, device_code, cell_code;
```

### `summary_sql_queries` — dữ liệu điền template

```sql
SELECT
    count() AS twinbeam_total_records,
    uniqExact(cell_code) AS twinbeam_affected_cells,
    round(avg(abs(delta_twb)), 2) AS twinbeam_avg_delta,
    round(max(abs(delta_twb)), 2) AS twinbeam_max_delta,
    round(avg(cqi), 2) AS twinbeam_avg_cqi,
    round(avg(ri1_rate), 2) AS twinbeam_avg_ri1_rate
FROM kpi_db.kpi_twinbeam_swap_fiber
WHERE date_hour >= toStartOfHour(now()) - INTERVAL 1 HOUR
  AND date_hour <  toStartOfHour(now())
  AND event_code IN {eventSessionCodes:Array(String)};
```

## 4. Blocked cell

### `sql_query` — dữ liệu chi tiết

```sql
SELECT
    date_hour,
    area_code,
    province_code,
    event_type,
    event_code,
    event_name,
    device_code,
    sector,
    cell_code,
    carrier_freq,
    total_mdt,
    ue_cell_distance_avg,
    distance,
    ps_traffic_gb,
    tu_dl_prb,
    dl_user_throughput_mbps,
    insert_ts
FROM kpi_db.kpi_blocked_cell
WHERE date_hour >= toStartOfHour(now()) - INTERVAL 1 HOUR
  AND date_hour <  toStartOfHour(now())
  AND event_code IN {eventSessionCodes:Array(String)}
ORDER BY date_hour, province_code, area_code, device_code, cell_code;
```

### `summary_sql_queries` — dữ liệu điền template

```sql
SELECT
    count() AS blocked_cell_total_records,
    uniqExact(cell_code) AS blocked_cell_affected_cells,
    sum(total_mdt) AS blocked_cell_total_mdt,
    round(avg(ps_traffic_gb), 2) AS blocked_cell_avg_ps_traffic_gb,
    round(avg(tu_dl_prb), 2) AS blocked_cell_avg_tu_dl_prb,
    round(avg(dl_user_throughput_mbps), 2) AS blocked_cell_avg_dl_throughput_mbps
FROM kpi_db.kpi_blocked_cell
WHERE date_hour >= toStartOfHour(now()) - INTERVAL 1 HOUR
  AND date_hour <  toStartOfHour(now())
  AND event_code IN {eventSessionCodes:Array(String)};
```

## 5. Azimuth station nhỏ hơn 20 độ

### `sql_query` — dữ liệu chi tiết

```sql
SELECT
    date_hour,
    area_code,
    province_code,
    event_type,
    event_code,
    event_name,
    device_code,
    sector,
    carrier_freq,
    cell_code_1,
    cell_code_2,
    cell_code_3,
    azi_predict_cell_1,
    azi_predict_cell_2,
    azi_predict_cell_3,
    delta_azi_cell_1_2,
    delta_azi_cell_1_3,
    delta_azi_cell_2_3,
    insert_ts
FROM kpi_db.kpi_azimuth_station_less_than_20_degrees
  WHERE date_hour >= toStartOfHour(now()) - INTERVAL 1 HOUR
    AND date_hour <  toStartOfHour(now())
  AND event_code IN {eventSessionCodes:Array(String)}
ORDER BY date_hour, province_code, area_code, device_code, cell_code_1;
```

### `summary_sql_queries` — dữ liệu điền template

```sql
SELECT
    count() AS azimuth_under_20_total_records,
    uniqExact(device_code) AS azimuth_under_20_affected_devices,
    uniqExact(cell_code_1) AS azimuth_under_20_affected_cells,
    round(avg(abs(delta_azi_cell_1_2)), 2) AS azimuth_under_20_avg_delta_1_2,
    round(max(greatest(abs(delta_azi_cell_1_2), abs(delta_azi_cell_1_3), abs(delta_azi_cell_2_3))), 2)
        AS azimuth_under_20_max_delta
FROM kpi_db.kpi_azimuth_station_less_than_20_degrees
WHERE date_hour >= toStartOfHour(now()) - INTERVAL 1 HOUR
  AND date_hour <  toStartOfHour(now())
  AND event_code IN {eventSessionCodes:Array(String)};
```

## 6. Coverage area by cell

### `sql_query` — dữ liệu chi tiết

```sql
SELECT
    date_hour,
    area_code,
    province_code,
    event_type,
    event_code,
    event_name,
    cell_code,
    sector,
    carrier_freq,
    avg_rsrp,
    avg_rsrq,
    total_zdt,
    percent_rsrp_greater_105dBm,
    percent_rsrp_greater_110dBm,
    percent_rsrp_greater_115dBm,
    percent_rsrp_greater_118dBm,
    percent_rsrp_greater_121dBm,
    percent_rsrp_under_120dBm,
    percent_rsrq_greater_12dBm,
    percent_rsrq_greater_14dBm,
    percent_rsrq_greater_16dBm,
    percent_rsrq_greater_18dBm,
    total_overlap,
    total_1_cell,
    percent_1_cell,
    total_2_cell,
    percent_2_cell,
    total_3_cell,
    percent_3_cell,
    total_4_cell,
    percent_4_cell,
    avg_cqi,
    percent_cqi_less_than_7,
    avg_rtwp,
    ps_traffic_gb,
    dl_ps_traffic_gb,
    ul_ps_traffic_gb,
    volte_traffic_erl,
    tu_dl_prb,
    tu_ul_prb,
    e_ps_cssr,
    e_ps_cdr,
    volte_cssr,
    dl_user_throughput_mbps,
    insert_ts
FROM kpi_db.kpi_coverage_area_by_cell
WHERE date_hour >= toStartOfHour(now()) - INTERVAL 1 HOUR
  AND date_hour <  toStartOfHour(now())
  AND event_code IN {eventSessionCodes:Array(String)}
ORDER BY date_hour, province_code, area_code, cell_code;
```

### `summary_sql_queries` — dữ liệu điền template

```sql
SELECT
    count() AS coverage_total_records,
    uniqExact(cell_code) AS coverage_affected_cells,
    round(avg(avg_rsrp), 2) AS coverage_avg_rsrp,
    round(avg(avg_rsrq), 2) AS coverage_avg_rsrq,
    round(sum(total_zdt), 2) AS coverage_total_zdt,
    round(avg(percent_rsrp_under_120dBm), 2) AS coverage_avg_rsrp_under_120_pct,
    round(avg(avg_cqi), 2) AS coverage_avg_cqi,
    round(avg(dl_user_throughput_mbps), 2) AS coverage_avg_dl_throughput_mbps
FROM kpi_db.kpi_coverage_area_by_cell
WHERE date_hour >= toStartOfHour(now()) - INTERVAL 1 HOUR
  AND date_hour <  toStartOfHour(now())
  AND event_code IN {eventSessionCodes:Array(String)};
```

## Điều kiện tích hợp bắt buộc

Hiện tại `BusinessRuleSummaryQueryService` chạy SQL cấu hình nguyên văn và chưa nhận `ProfileEvent`/`payload.eventSessionCodes`; vì vậy nó chưa thể bind `{eventSessionCodes:Array(String)}`. Cần truyền mảng này từ `ProfileEvent.payload` đến service truy vấn, sau đó gửi nó như ClickHouse HTTP parameter. Không nối chuỗi mã session trực tiếp vào SQL.

Nếu message không có session code hoặc mảng rỗng, không thực thi SQL (hoặc trả về tập kết quả rỗng) để tránh truy vấn toàn bộ dữ liệu KPI của giờ trước.
