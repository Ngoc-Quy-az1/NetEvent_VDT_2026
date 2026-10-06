package com.example.notification.adapter.infrastructure.repository;

import com.example.notification.adapter.domain.ProfileEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProfileRepository extends JpaRepository<ProfileEntity, UUID> {
    Optional<ProfileEntity> findByProfileName(String profileName);
    
    List<ProfileEntity> findByStatus(String status);

    @Query("SELECT p FROM ProfileEntity p " +
           "WHERE p.status = 'ACTIVE' " +
           "AND p.cronExpression IS NOT NULL " +
           "AND (p.startTime IS NULL OR p.startTime <= :now) " +
           "AND (p.endTime IS NULL OR p.endTime >= :now)")
    List<ProfileEntity> findEligibleProfilesForTrigger(@Param("now") Instant now);

    @Query(value = "SELECT pg_try_advisory_xact_lock(hashtext('notification-adapter-profile-trigger'))", nativeQuery = true)
    Boolean tryAcquireTriggerLock();
}
