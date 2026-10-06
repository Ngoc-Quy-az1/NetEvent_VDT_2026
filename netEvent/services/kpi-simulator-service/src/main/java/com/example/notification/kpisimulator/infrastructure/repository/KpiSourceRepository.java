package com.example.notification.kpisimulator.infrastructure.repository;

import com.example.notification.common.query.SqlIdentifier;
import com.example.notification.common.query.SqlSelectBuilder;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/** PostgreSQL read-side queries used as inputs for KPI simulation. */
@Repository
public class KpiSourceRepository {
    private final JdbcTemplate jdbc;

    public KpiSourceRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<Map<String, Object>> findEventSessions() {
        return jdbc.queryForList(new SqlSelectBuilder()
                .select("es.session_event_code", "e.event_name", "COALESCE(et.event_type_code, 'khac') AS event_type")
                .from(SqlIdentifier.of("event_session"), "es")
                .join("INNER", SqlIdentifier.of("event"), "e", "e.event_id = es.event_id")
                .join("LEFT", SqlIdentifier.of("event_type"), "et", "et.event_type_id = e.event_type_id")
                .orderBy("es.session_event_code").build());
    }

    public List<Map<String, Object>> findNetworkSources() {
        return jdbc.queryForList(new SqlSelectBuilder()
                .select("s.area_code", "s.province_code", "c.cell_code", "c.device_code",
                        "COALESCE(c.sector, 'S1') AS sector", "COALESCE(c.network, 'Band_1800') AS carrier_freq")
                .from(SqlIdentifier.of("cell"), "c")
                .join("INNER", SqlIdentifier.of("site"), "s", "s.site_id = c.site_id")
                .orderBy("c.cell_code").build());
    }
}
