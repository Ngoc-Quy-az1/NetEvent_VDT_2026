package com.example.notification.kpisimulator.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.example.notification.kpisimulator.infrastructure.repository.KpiSourceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

@Service
@Slf4j
@RequiredArgsConstructor
public class KpiSimulationService {
    private static final DateTimeFormatter CLICKHOUSE_DATE = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.of("Asia/Ho_Chi_Minh"));
    private final KpiSourceRepository kpiSourceRepository;
    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${kpi.simulator.clickhouse-url:http://localhost:8123}")
    private String clickHouseUrl;

    @Value("${kpi.simulator.clickhouse-username:default}")
    private String clickHouseUsername;

    @Value("${kpi.simulator.clickhouse-password:clickhousepassword}")
    private String clickHousePassword;

    public void simulateCurrentHour() {
        Instant hour = Instant.now().truncatedTo(ChronoUnit.HOURS);
        List<Map<String, Object>> sessions = loadEventSessions();
        if (sessions.isEmpty()) {
            log.info("No event sessions found for KPI simulation at {}", hour);
            return;
        }
        List<Map<String, Object>> networkSources = loadNetworkSources();
        for (KpiTable table : KpiTable.values()) {
            List<Map<String, Object>> rows = new ArrayList<>();
            for (int sessionIndex = 0; sessionIndex < sessions.size(); sessionIndex++) {
                Map<String, Object> session = sessions.get(sessionIndex);
                // A network cell must belong to one session only. The same source
                // is reused for all KPI tables of that session, but never for a
                // different session in this simulation run.
                rows.add(rowFor(table, session, sourceFor(session, sessionIndex, networkSources), hour));
            }
            insert(table, rows);
        }
        log.info("Generated one random KPI row for each of {} event session(s), six ClickHouse tables using {} distinct network source(s)",
                sessions.size(), Math.max(1, networkSources.size()));
    }

    private List<Map<String, Object>> loadEventSessions() {
        // Do not join profile_event_session or cell_session: those joins duplicate
        // a session (one row per profile/cell) and make the generated row count
        // differ from the number of event_session_code records.
        return kpiSourceRepository.findEventSessions();
    }

    private List<Map<String, Object>> loadNetworkSources() {
        return kpiSourceRepository.findNetworkSources();
    }

    private Map<String, Object> sourceFor(Map<String, Object> session, int sessionIndex,
                                          List<Map<String, Object>> networkSources) {
        if (sessionIndex < networkSources.size()) {
            // Both lists are ordered by their natural code. Pairing by index is
            // deterministic and prevents the hash collisions of the old mapping.
            return networkSources.get(sessionIndex);
        }

        // Do not reuse a real cell when there are more event sessions than source
        // cells. A unique synthetic cell preserves the one-cell/one-session rule.
        String code = value(session, "session_event_code");
        Map<String, Object> fallback = new LinkedHashMap<>();
        fallback.put("area_code", areaCodeFor(code));
        fallback.put("province_code", "SIM");
        fallback.put("cell_code", "SIM_" + code);
        fallback.put("device_code", "SIM_DEVICE_" + sessionIndex);
        fallback.put("sector", "S1");
        fallback.put("carrier_freq", "Band_1800");
        return fallback;
    }

    private String areaCodeFor(String sessionCode) {
        if (sessionCode.startsWith("KV1")) return "KV1";
        if (sessionCode.startsWith("KV2")) return "KV2";
        if (sessionCode.startsWith("KV3")) return "KV3";
        return "SIM";
    }

    private Map<String, Object> rowFor(KpiTable table, Map<String, Object> session, Map<String, Object> source, Instant hour) {
        Map<String, Object> row = common(session, source, hour);
        String cell = value(session, "cell_code");
        double random = ThreadLocalRandom.current().nextDouble();
        switch (table) {
            case OVERSHOOT_CELL:
                row.put("number_overshoot_sample", 50 + random * 100); row.put("total_mdt_sample", 150 + random * 150); row.put("overshoot_rate", 20 + random * 45); break;
            case AZIMUTH_SWAP_FEEDER:
                row.put("number_ue_have_mdt", 100 + random * 400); row.put("azi_nims", random * 360); row.put("azi_predict", random * 360); row.put("delta_azi", 20 + random * 120); row.put("type", "Sai azimuth"); break;
            case TWINBEAM_SWAP_FIBER:
                row.put("cell_twinbeam", cell + "_TWIN"); row.put("azi_predict", random * 360); row.put("delta_twb", 10 + random * 80); row.put("cqi", 4 + random * 10); row.put("ri1_rate", random * 100); break;
            case BLOCKED_CELL:
                row.put("total_mdt", (long) (100 + random * 900)); row.put("ue_cell_distance_avg", 500 + random * 3000); row.put("distance", 1000 + random * 5000); row.put("ps_traffic_gb", random * 200); row.put("tu_dl_prb", random * 100); row.put("dl_user_throughput_mbps", 2 + random * 80); break;
            case AZIMUTH_STATION_LESS_THAN_20_DEGREES:
                row.remove("cell_code"); row.put("cell_code_1", cell); row.put("cell_code_2", cell + "_2"); row.put("cell_code_3", cell + "_3"); row.put("azi_predict_cell_1", random * 360); row.put("azi_predict_cell_2", random * 360); row.put("azi_predict_cell_3", random * 360); row.put("delta_azi_cell_1_2", random * 20); row.put("delta_azi_cell_1_3", random * 20); row.put("delta_azi_cell_2_3", random * 20); break;
            default:
                coverage(row, random);
        }
        return row;
    }

    private Map<String, Object> common(Map<String, Object> session, Map<String, Object> source, Instant hour) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("date_hour", CLICKHOUSE_DATE.format(hour));
        String sessionCode = value(session, "session_event_code");
        // Event identifiers come from PostgreSQL. Network dimensions use an
        // existing site/cell source selected consistently for this session.
        row.put("area_code", value(source, "area_code")); row.put("province_code", value(source, "province_code"));
        row.put("event_type", value(session, "event_type"));
        row.put("event_code", sessionCode);
        row.put("event_name", value(session, "event_name"));
        row.put("device_code", value(source, "device_code")); row.put("sector", value(source, "sector"));
        row.put("cell_code", value(source, "cell_code")); row.put("carrier_freq", value(source, "carrier_freq"));
        row.put("insert_ts", Instant.now().toEpochMilli());
        return row;
    }

    private void coverage(Map<String, Object> row, double random) {
        row.put("avg_rsrp", -75 - random * 35); row.put("avg_rsrq", -6 - random * 12); row.put("total_zdt", 100 + random * 1000);
        row.put("percent_rsrp_greater_105dBm", 60 + random * 30); row.put("percent_rsrp_greater_110dBm", 40 + random * 30); row.put("percent_rsrp_greater_115dBm", 20 + random * 25); row.put("percent_rsrp_greater_118dBm", 10 + random * 15); row.put("percent_rsrp_greater_121dBm", random * 10); row.put("percent_rsrp_under_120dBm", random * 20);
        row.put("percent_rsrq_greater_12dBm", 60 + random * 20); row.put("percent_rsrq_greater_14dBm", 40 + random * 20); row.put("percent_rsrq_greater_16dBm", 20 + random * 20); row.put("percent_rsrq_greater_18dBm", random * 15); row.put("total_overlap", random * 100);
        row.put("total_1_cell", 100 + random * 300); row.put("percent_1_cell", 20 + random * 50); row.put("total_2_cell", 100 + random * 300); row.put("percent_2_cell", 20 + random * 50); row.put("total_3_cell", 50 + random * 200); row.put("percent_3_cell", random * 30); row.put("total_4_cell", random * 100); row.put("percent_4_cell", random * 15);
        row.put("avg_cqi", 4 + random * 10); row.put("percent_cqi_less_than_7", random * 50); row.put("avg_rtwp", -115 + random * 15); row.put("ps_traffic_gb", random * 200); row.put("dl_ps_traffic_gb", random * 160); row.put("ul_ps_traffic_gb", random * 40); row.put("volte_traffic_erl", random * 100); row.put("tu_dl_prb", random * 100); row.put("tu_ul_prb", random * 100); row.put("e_ps_cssr", 90 + random * 10); row.put("e_ps_cdr", random * 5); row.put("volte_cssr", 90 + random * 10); row.put("dl_user_throughput_mbps", 2 + random * 80);
    }

    private void insert(KpiTable table, List<Map<String, Object>> rows) {
        if (rows.isEmpty()) return;
        try {
            StringBuilder body = new StringBuilder();
            for (Map<String, Object> row : rows) body.append(objectMapper.writeValueAsString(row)).append('\n');
            String query = URLEncoder.encode("INSERT INTO kpi_db." + table.tableName + " FORMAT JSONEachRow", StandardCharsets.UTF_8);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBasicAuth(clickHouseUsername, clickHousePassword, StandardCharsets.UTF_8);
            restTemplate.postForEntity(clickHouseUrl + "?query=" + query, new HttpEntity<>(body.toString(), headers), String.class);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Could not serialize rows for " + table.tableName, exception);
        }
    }

    private String value(Map<String, Object> row, String key) { return String.valueOf(row.getOrDefault(key, "UNKNOWN")); }

    /** Trusted SQL identifiers; never map this from HTTP input. */
    private enum KpiTable {
        OVERSHOOT_CELL("kpi_overshoot_cell"),
        AZIMUTH_SWAP_FEEDER("kpi_azimuth_swap_feeder"),
        TWINBEAM_SWAP_FIBER("kpi_twinbeam_swap_fiber"),
        BLOCKED_CELL("kpi_blocked_cell"),
        AZIMUTH_STATION_LESS_THAN_20_DEGREES("kpi_azimuth_station_less_than_20_degrees"),
        COVERAGE_AREA_BY_CELL("kpi_coverage_area_by_cell");

        private final String tableName;

        KpiTable(String tableName) {
            this.tableName = tableName;
        }
    }
}
