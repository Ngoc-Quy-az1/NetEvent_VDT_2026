package com.example.notification.adapter.domain;

import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "holiday_occasion")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HolidayOccasionEntity {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "holiday_id", updatable = false, nullable = false)
    private UUID holidayId;

    @Column(name = "holiday_code", nullable = false, unique = true)
    private String holidayCode;

    @Column(name = "holiday_name", nullable = false)
    private String holidayName;

    @OneToMany(mappedBy = "holidayOccasion")
    @Builder.Default
    private List<EventDefinitionEntity> events = new ArrayList<>();

    public void addEvent(EventDefinitionEntity event) {
        events.add(event);
        event.setHolidayOccasion(this);
    }

    public void removeEvent(EventDefinitionEntity event) {
        events.remove(event);
        event.setHolidayOccasion(null);
    }
}
