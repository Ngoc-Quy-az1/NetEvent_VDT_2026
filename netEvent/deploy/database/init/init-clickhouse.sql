
CREATE DATABASE IF NOT EXISTS kpi_db;

USE kpi_db;


-- 1. CELL OVERSHOOT

CREATE TABLE IF NOT EXISTS kpi_db.kpi_overshoot_cell
(
    date_hour DateTime,
    area_code String,
    province_code String,
    event_type String,
    event_code String,
    event_name String,
    cell_code String,
    device_code String,
    sector String,
    carrier_freq String,

    number_overshoot_sample Float64,
    total_mdt_sample Float64,
    overshoot_rate Float64,

    insert_ts Int64
)
ENGINE = MergeTree()
ORDER BY
(
    date_hour,
    province_code,
    area_code,
    cell_code
);



-- 2. SAI AZIMUTH AND SWAP FEEDER

CREATE TABLE IF NOT EXISTS kpi_db.kpi_azimuth_swap_feeder
(
    date_hour DateTime,
    area_code String,
    province_code String,
    event_type String,
    event_code String,
    event_name String,
    device_code String,
    sector String,
    cell_code String,
    carrier_freq String,

    number_ue_have_mdt Float64,
    azi_nims Float64,
    azi_predict Float64,
    delta_azi Float64,
    type String,

    insert_ts Int64
)
ENGINE = MergeTree()
ORDER BY
(
    date_hour,
    province_code,
    area_code,
    device_code,
    cell_code
);



-- 3. CELL TWINBEAM SWAP

CREATE TABLE IF NOT EXISTS kpi_db.kpi_twinbeam_swap_fiber
(
    date_hour DateTime,
    area_code String,
    province_code String,
    event_type String,
    event_code String,
    event_name String,
    device_code String,
    sector String,
    cell_code String,
    cell_twinbeam String,
    carrier_freq String,

    azi_predict Float64,
    delta_twb Float64,
    cqi Float64,
    ri1_rate Float64,

    insert_ts Int64
)
ENGINE = MergeTree()
ORDER BY
(
    date_hour,
    province_code,
    area_code,
    device_code,
    cell_code
);


-- 4. BLOCKED CELL

CREATE TABLE IF NOT EXISTS kpi_db.kpi_blocked_cell
(
    date_hour DateTime,
    area_code String,
    province_code String,
    event_type String,
    event_code String,
    event_name String,
    device_code String,
    sector String,
    cell_code String,
    carrier_freq String,

    total_mdt Int64,
    ue_cell_distance_avg Float64,
    distance Float64,
    ps_traffic_gb Float64,
    tu_dl_prb Float64,
    dl_user_throughput_mbps Float64,

    insert_ts Float64
)
ENGINE = MergeTree()
ORDER BY
(
    date_hour,
    province_code,
    area_code,
    device_code,
    cell_code
);



-- 5. AZIMUTH STATION LESS THAN 20 DEGREES

CREATE TABLE IF NOT EXISTS kpi_db.kpi_azimuth_station_less_than_20_degrees
(
    date_hour DateTime,
    area_code String,
    province_code String,
    event_type String,
    event_code String,
    event_name String,
    device_code String,
    sector String,
    carrier_freq String,

    cell_code_1 String,
    cell_code_2 String,
    cell_code_3 String,

    azi_predict_cell_1 Float64,
    azi_predict_cell_2 Float64,
    azi_predict_cell_3 Float64,

    delta_azi_cell_1_2 Float64,
    delta_azi_cell_1_3 Float64,
    delta_azi_cell_2_3 Float64,

    insert_ts Int64
)
ENGINE = MergeTree()
ORDER BY
(
    date_hour,
    province_code,
    area_code,
    device_code,
    cell_code_1
);



-- 6. COVERAGE AREA BY CELL

CREATE TABLE IF NOT EXISTS kpi_db.kpi_coverage_area_by_cell
(
    date_hour DateTime,
    area_code String,
    province_code String,
    event_type String,
    event_code String,
    event_name String,
    cell_code String,
    sector String,
    carrier_freq String,

    avg_rsrp Float64,
    avg_rsrq Float64,

    total_zdt Float64,

    percent_rsrp_greater_105dBm Float64,
    percent_rsrp_greater_110dBm Float64,
    percent_rsrp_greater_115dBm Float64,
    percent_rsrp_greater_118dBm Float64,
    percent_rsrp_greater_121dBm Float64,

    percent_rsrp_under_120dBm Float64,

    percent_rsrq_greater_12dBm Float64,
    percent_rsrq_greater_14dBm Float64,
    percent_rsrq_greater_16dBm Float64,
    percent_rsrq_greater_18dBm Float64,

    total_overlap Float64,

    total_1_cell Float64,
    percent_1_cell Float64,

    total_2_cell Float64,
    percent_2_cell Float64,

    total_3_cell Float64,
    percent_3_cell Float64,

    total_4_cell Float64,
    percent_4_cell Float64,

    avg_cqi Float64,
    percent_cqi_less_than_7 Float64,

    avg_rtwp Float64,

    ps_traffic_gb Float64,
    dl_ps_traffic_gb Float64,
    ul_ps_traffic_gb Float64,

    volte_traffic_erl Float64,

    tu_dl_prb Float64,
    tu_ul_prb Float64,

    e_ps_cssr Float64,
    e_ps_cdr Float64,
    volte_cssr Float64,

    dl_user_throughput_mbps Float64,

    insert_ts Int64
)
ENGINE = MergeTree()
ORDER BY
(
    date_hour,
    province_code,
    area_code,
    cell_code
);