package com.example.notification.adapter.infrastructure.repository;

import com.example.notification.adapter.domain.BusinessRuleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BusinessRuleRepository extends JpaRepository<BusinessRuleEntity, UUID> {
    Optional<BusinessRuleEntity> findByBusinessRuleCode(String businessRuleCode);

    @Query("SELECT br FROM BusinessRuleEntity br " +
           "JOIN br.profileMappings mapping " +
           "WHERE mapping.profileId = :profileId " +
           "AND mapping.status = 'ACTIVE' " +
           "AND br.status = 'ACTIVE'")
    List<BusinessRuleEntity> findByProfileId(@Param("profileId") UUID profileId);
}
