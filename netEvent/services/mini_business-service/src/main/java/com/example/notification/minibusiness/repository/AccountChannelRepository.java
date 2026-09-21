package com.example.notification.minibusiness.repository;

import com.example.notification.minibusiness.domain.entity.AccountChannelEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AccountChannelRepository extends JpaRepository<AccountChannelEntity, UUID> {
    List<AccountChannelEntity> findByAccountId(UUID accountId);
    List<AccountChannelEntity> findByAccountIdAndIsActiveTrue(UUID accountId);
}
