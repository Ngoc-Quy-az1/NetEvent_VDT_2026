package com.example.notification.adapter.infrastructure.repository;

import com.example.notification.common.query.SqlIdentifier;
import com.example.notification.common.query.SqlSelectBuilder;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Read model query for profile groups; SQL remains infrastructure-only. */
@Repository
public class ProfileGroupQueryRepository {
    private final NamedParameterJdbcTemplate jdbc;

    public ProfileGroupQueryRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<Map<String, Object>> findByProfileId(UUID profileId) {
        return jdbc.queryForList(new SqlSelectBuilder().select("group_id", "channel_id", "group_config")
                        .from(SqlIdentifier.of("profile_group"), null).where("profile_id = :profileId").build(),
                new MapSqlParameterSource("profileId", profileId));
    }
}
