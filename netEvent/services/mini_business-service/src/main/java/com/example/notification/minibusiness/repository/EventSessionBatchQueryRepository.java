package com.example.notification.minibusiness.repository;

import com.example.notification.common.query.SqlIdentifier;
import com.example.notification.common.query.SqlSelectBuilder;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * JDBC queries required by the batch workflow. Table and column identifiers are
 * constants owned by this repository; callers can supply values only.
 */
@Repository
public class EventSessionBatchQueryRepository {
    private static final SqlIdentifier EVENT_TABLE = SqlIdentifier.of("event");
    private static final SqlIdentifier EVENT_SESSION_TABLE = SqlIdentifier.of("event_session");
    private static final SqlIdentifier CELL_TABLE = SqlIdentifier.of("cell");
    private static final SqlIdentifier SITE_TABLE = SqlIdentifier.of("site");

    private final NamedParameterJdbcTemplate jdbc;

    public EventSessionBatchQueryRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<Map<String, Object>> findEventById(UUID eventId) {
        return jdbc.queryForList(new SqlSelectBuilder().select("event_id", "event_code", "event_name", "status")
                        .from(EVENT_TABLE, null).where("event_id = :eventId").build(),
                new MapSqlParameterSource("eventId", eventId));
    }

    public boolean sessionCodeExists(String sessionEventCode) {
        Integer count = jdbc.queryForObject(new SqlSelectBuilder().select("count(*)")
                        .from(EVENT_SESSION_TABLE, null).where("session_event_code = :sessionEventCode").build(),
                new MapSqlParameterSource("sessionEventCode", sessionEventCode), Integer.class);
        return count != null && count > 0;
    }

    public List<UUID> findCellIdsBySiteCode(String siteCode) {
        if (siteCode == null || siteCode.trim().isEmpty()) {
            return Collections.emptyList();
        }
        String normalized = siteCode.trim();
        String sql = new SqlSelectBuilder().select("c.cell_id").from(CELL_TABLE, "c")
                .join("INNER", SITE_TABLE, "s", "c.site_id = s.site_id")
                .where("(s.station_code = :exact OR s.station_code ILIKE :prefix OR s.station_code ILIKE :suffix OR s.station_code ILIKE :contains)")
                .build();
        MapSqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("exact", normalized)
                .addValue("prefix", normalized + "%")
                .addValue("suffix", "%" + normalized)
                .addValue("contains", "%" + normalized + "%");
        return jdbc.query(sql, parameters, (rs, rowNum) -> UUID.fromString(rs.getString("cell_id")));
    }

    public void insertSessions(List<Object[]> rows) {
        if (rows.isEmpty()) return;
        jdbc.getJdbcTemplate().batchUpdate("INSERT INTO " + EVENT_SESSION_TABLE + " ("
                        + "session_id, session_event_code, event_id, start_date, end_date, lunar_start_date, lunar_end_date, "
                        + "expected_participants, status, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, 'PLANNED', now(), now())",
                rows);
    }

    public void insertCellSessions(List<Object[]> rows) {
        if (rows.isEmpty()) return;
        jdbc.getJdbcTemplate().batchUpdate("INSERT INTO cell_session (cell_session_id, cell_id, session_id, created_at, updated_at) "
                        + "VALUES (?, ?, ?, now(), now()) ON CONFLICT (cell_id, session_id) DO NOTHING", rows);
    }

    public List<Map<String, Object>> findEventTypes() {
        return jdbc.queryForList(new SqlSelectBuilder().select("event_type_id", "event_type_code", "event_type_name")
                        .from(SqlIdentifier.of("event_type"), null).orderBy("event_type_name").build(),
                new MapSqlParameterSource());
    }

    public List<Map<String, Object>> findHolidays() {
        return jdbc.queryForList(new SqlSelectBuilder().select("holiday_id", "holiday_code", "holiday_name")
                        .from(SqlIdentifier.of("holiday_occasion"), null).orderBy("holiday_name").build(),
                new MapSqlParameterSource());
    }
}
