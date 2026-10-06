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
}
