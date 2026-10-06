package com.example.notification.processor.entity;

import com.vladmihalcea.hibernate.type.array.StringArrayType;
import lombok.*;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import javax.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "business_rule")
@TypeDef(name = "string-array", typeClass = StringArrayType.class)
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class BusinessRule {
    @Id @Column(name = "business_rule_id", nullable = false, updatable = false) private UUID businessRuleId;
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
    @Column(name = "source_password") private String sourcePassword;
    @Type(type = "string-array") @Column(name = "sql_query", columnDefinition = "text[]", nullable = false) private String[] sqlQuery;
    @Type(type = "string-array") @Column(name = "summary_sql_queries", columnDefinition = "text[]", nullable = false) private String[] summarySqlQueries;
    @Column(name = "description") private String description;
    @Column(name = "status", nullable = false) private String status;
    @Column(name = "created_at", nullable = false, updatable = false) private Instant createdAt;
    @Column(name = "updated_at", nullable = false) private Instant updatedAt;

    @PrePersist void beforeInsert() {
        Instant now = Instant.now();
        if (sqlQuery == null) sqlQuery = new String[0];
        if (summarySqlQueries == null) summarySqlQueries = new String[0];
        if (status == null) status = "ACTIVE";
        if (createdAt == null) createdAt = now;
        if (updatedAt == null) updatedAt = now;
    }
    @PreUpdate void beforeUpdate() { updatedAt = Instant.now(); }
}
