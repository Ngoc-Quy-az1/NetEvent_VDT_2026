package com.example.notification.minibusiness.repository;

import com.example.notification.minibusiness.domain.entity.ProfileBusinessRuleMappingEntity;
import com.example.notification.minibusiness.domain.entity.ProfileBusinessRuleMappingId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ProfileBusinessRuleMappingRepository extends JpaRepository<ProfileBusinessRuleMappingEntity, ProfileBusinessRuleMappingId> {
    List<ProfileBusinessRuleMappingEntity> findByProfileId(UUID profileId);
    List<ProfileBusinessRuleMappingEntity> findByBusinessRuleId(UUID businessRuleId);
    void deleteByProfileId(UUID profileId);
}
