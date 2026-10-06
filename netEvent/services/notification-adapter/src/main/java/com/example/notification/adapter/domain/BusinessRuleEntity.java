package com.example.notification.adapter.domain;

import lombok.*;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Type;
import javax.persistence.*;
import java.time.Instant;
import java.util.*;

@Entity
@Table(name = "business_rule")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class BusinessRuleEntity {
    @Id @GeneratedValue(generator = "UUID") @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "business_rule_id", updatable = false, nullable = false) private UUID businessRuleId;
    @Column(name = "business_rule_code", nullable = false, unique = true) private String businessRuleCode;
    @Column(name = "business_rule_name", nullable = false) private String businessRuleName;
    @Column(name = "source_db_type") private String sourceDbType;
    @Column(name = "source_host") private String sourceHost;
    @Column(name = "source_port") private Integer sourcePort;
    @Column(name = "source_database") private String sourceDatabase;
    @Column(name = "source_schema") private String sourceSchema;
    @Column(name = "source_table") private String sourceTable;
    @Column(name = "source_connection_ref") private String sourceConnectionRef;
    @Column(name = "source_username") private String sourceUsername;
    @Column(name = "encrypted_password") private String encryptedPassword;
    @Type(type = "string-array") @Column(name = "sql_query", nullable = false, columnDefinition = "text[]") private String[] sqlQueries;
    @Type(type = "string-array") @Column(name = "summary_sql_queries", nullable = false, columnDefinition = "text[]") private String[] summarySqlQueries;
    @Column(name = "description") private String description;
    @Column(name = "status", nullable = false) private String status;
    @Column(name = "created_at", nullable = false, updatable = false) private Instant createdAt;
    @Column(name = "updated_at", nullable = false) private Instant updatedAt;
    @OneToMany(mappedBy = "businessRule") @Builder.Default private List<ProfileBusinessRuleMappingEntity> profileMappings = new ArrayList<>();
    @PrePersist void prePersist() { if (status == null) status = "ACTIVE"; if (sqlQueries == null) sqlQueries = new String[0]; if (summarySqlQueries == null) summarySqlQueries = new String[0]; if (createdAt == null) createdAt = Instant.now(); if (updatedAt == null) updatedAt = Instant.now(); }
    @PreUpdate void preUpdate() { updatedAt = Instant.now(); }
}
