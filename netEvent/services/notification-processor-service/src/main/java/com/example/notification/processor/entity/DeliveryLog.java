package com.example.notification.processor.entity;

import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "delivery_log")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class DeliveryLog {
    @Id @GeneratedValue(generator = "UUID") @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "delivery_log_id", nullable = false, updatable = false) private UUID deliveryLogId;
    @Column(name = "task_id", nullable = false) private UUID taskId;
    @Column(name = "channel_code") private String channelCode;
    @Column(name = "recipient_contact") private String recipientContact;
    @Column(name = "sent_at") private Instant sentAt;
    @Column(name = "status", nullable = false) private String status;
    @Column(name = "provider_response_code") private String providerResponseCode;
    @Column(name = "provider_response_body") private String providerResponseBody;
    @Column(name = "error_message") private String errorMessage;
    @Column(name = "created_at", nullable = false, updatable = false) private Instant createdAt;
    @Column(name = "correlation_id") private UUID correlationId;

    @PrePersist void beforeInsert() { if (createdAt == null) createdAt = Instant.now(); }
}
