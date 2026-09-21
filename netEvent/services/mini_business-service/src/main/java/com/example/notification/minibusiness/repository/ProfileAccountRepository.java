package com.example.notification.minibusiness.repository;

import com.example.notification.minibusiness.domain.entity.ProfileAccountEntity;
import com.example.notification.minibusiness.domain.entity.ProfileAccountId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ProfileAccountRepository extends JpaRepository<ProfileAccountEntity, ProfileAccountId> {
    List<ProfileAccountEntity> findByProfileId(UUID profileId);
    List<ProfileAccountEntity> findByAccountId(UUID accountId);
    void deleteByProfileId(UUID profileId);
}
