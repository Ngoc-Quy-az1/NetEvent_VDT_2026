package com.example.notification.minibusiness.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
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
}
