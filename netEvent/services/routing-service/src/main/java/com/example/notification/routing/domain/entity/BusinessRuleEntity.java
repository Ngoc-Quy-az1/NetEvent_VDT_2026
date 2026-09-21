package com.example.notification.routing.domain.entity;

import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "business_rule")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BusinessRuleEntity {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "business_rule_id", updatable = false, nullable = false)
    private UUID businessRuleId;

    @Column(name = "business_rule_code", nullable = false, unique = true)
    private String businessRuleCode;

    @Column(name = "business_rule_name", nullable = false)
    private String businessRuleName;

    @Column(name = "source_db_type")
    private String sourceDbType;

    @Column(name = "source_host")
    private String sourceHost;

    @Column(name = "source_port")
    private Integer sourcePort;

    @Column(name = "source_database")
    private String sourceDatabase;

    @Column(name = "source_schema")
    private String sourceSchema;

    @Column(name = "source_table")
    private String sourceTable;

    @Column(name = "source_connection_ref")
    private String sourceConnectionRef;

    @Column(name = "sql_query", nullable = false, columnDefinition = "TEXT")
    private String sqlQuery;

    @Column(name = "description")
    private String description;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @OneToMany(mappedBy = "businessRule", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ProfileBusinessRuleMappingEntity> profileMappings = new ArrayList<>();

    public void addProfileMapping(ProfileBusinessRuleMappingEntity mapping) {
        profileMappings.add(mapping);
        mapping.setBusinessRule(this);
    }

    public void removeProfileMapping(ProfileBusinessRuleMappingEntity mapping) {
        profileMappings.remove(mapping);
        mapping.setBusinessRule(null);
    }
}
