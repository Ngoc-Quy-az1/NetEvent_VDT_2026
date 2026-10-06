package com.example.notification.processor.entity;

import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import javax.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity @Table(name = "recipient") @TypeDef(name = "jsonb", typeClass = JsonBinaryType.class)
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class Recipient {
    @Id @GeneratedValue(generator = "UUID") @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "recipient_id", nullable = false, updatable = false) private UUID recipientId;
    @Column(name = "account_id") private UUID accountId;
    @Column(name = "full_name", nullable = false) private String fullName;
    @Column(name = "phone_number") private String phoneNumber;
    @Type(type = "jsonb") @Column(name = "channel_config", columnDefinition = "jsonb", nullable = false) private String channelConfig;
    @Column(name = "status", nullable = false) private String status;
    @Column(name = "created_at", nullable = false, updatable = false) private Instant createdAt;
    @Column(name = "updated_at", nullable = false) private Instant updatedAt;

    @PrePersist
    void beforeInsert() {
        Instant now = Instant.now();
        if (createdAt == null) createdAt = now;
        if (updatedAt == null) updatedAt = now;
        if (channelConfig == null) channelConfig = "{}";
        if (status == null) status = "ACTIVE";
    }

    @PreUpdate
    void beforeUpdate() {
        updatedAt = Instant.now();
    }
}
