package com.example.notification.adapter.domain;

import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "event_type")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventTypeEntity {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "event_type_id", updatable = false, nullable = false)
    private UUID eventTypeId;

    @Column(name = "event_type_code", nullable = false, unique = true)
    private String eventTypeCode;

    @Column(name = "event_type_name", nullable = false)
    private String eventTypeName;

    @OneToMany(mappedBy = "eventType")
    @Builder.Default
    private List<EventDefinitionEntity> events = new ArrayList<>();

    public void addEvent(EventDefinitionEntity event) {
        events.add(event);
        event.setEventType(this);
    }

    public void removeEvent(EventDefinitionEntity event) {
        events.remove(event);
        event.setEventType(null);
    }
}
