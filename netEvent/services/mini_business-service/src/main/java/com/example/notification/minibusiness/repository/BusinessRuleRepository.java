package com.example.notification.minibusiness.repository;

import com.example.notification.minibusiness.domain.entity.BusinessRuleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface BusinessRuleRepository extends JpaRepository<BusinessRuleEntity, UUID> {
    Optional<BusinessRuleEntity> findByBusinessRuleCode(String businessRuleCode);
    boolean existsByBusinessRuleCode(String businessRuleCode);
}
