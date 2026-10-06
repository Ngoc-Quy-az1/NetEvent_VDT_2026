package com.example.notification.processor.entity;

import lombok.*;
import javax.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity @Table(name = "processed_event")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class ProcessedEvent {
    @Id @Column(name = "event_id") private UUID eventId;
    @Column(name = "consumer_name", nullable = false) private String consumerName;
    @Column(name = "event_type", nullable = false) private String eventType;
    @Column(name = "processed_at", nullable = false) private Instant processedAt;
}
