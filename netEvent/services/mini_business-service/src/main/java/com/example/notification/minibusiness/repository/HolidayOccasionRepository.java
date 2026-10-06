package com.example.notification.minibusiness.repository;

import com.example.notification.minibusiness.domain.entity.HolidayOccasionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface HolidayOccasionRepository extends JpaRepository<HolidayOccasionEntity, UUID> {
    Optional<HolidayOccasionEntity> findByHolidayCode(String holidayCode);
}
