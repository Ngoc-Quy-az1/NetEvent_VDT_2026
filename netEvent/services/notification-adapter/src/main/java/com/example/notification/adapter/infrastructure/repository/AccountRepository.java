package com.example.notification.adapter.infrastructure.repository;

import com.example.notification.adapter.application.dto.AccountDTO;
import com.example.notification.adapter.domain.AccountEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AccountRepository extends JpaRepository<AccountEntity, UUID> {

    @Query("SELECT new com.example.notification.adapter.application.dto.AccountDTO(" +
           "    a.accountId, " +
           "    a.username, " +
           "    a.fullName, " +
           "    a.areaCode, " +
           "    a.roleId, " +
           "    a.cellPhone, " +
           "    ac.channelId, " +
           "    ac.contactValue, " +
           "    ac.label, " +
           "    ac.isActive, " +
           "    ac.config, " +
           "    ac.accountChannelId" +
           ") " +
           "FROM ProfileAccountEntity pa " +
           "JOIN pa.account a " +
           "JOIN a.accountChannels ac " +
           "WHERE pa.profileId = :profileId " +
           "  AND ac.isActive = true")
    List<AccountDTO> findAccountsByProfileId(@Param("profileId") UUID profileId);

    @Query("SELECT new com.example.notification.adapter.application.dto.AccountDTO(" +
           "    a.accountId, " +
           "    a.username, " +
           "    a.fullName, " +
           "    a.areaCode, " +
           "    a.roleId, " +
           "    a.cellPhone, " +
           "    ac.channelId, " +
           "    ac.contactValue, " +
           "    ac.label, " +
           "    ac.isActive, " +
           "    ac.config, " +
           "    ac.accountChannelId" +
           ") " +
           "FROM ProfileAccountEntity pa " +
           "JOIN pa.account a " +
           "JOIN a.accountChannels ac " +
           "WHERE pa.profileId = :profileId " +
           "  AND ac.isActive = true " +
           "  AND ac.channelId IN (" +
           "      SELECT pc.channelId FROM ProfileChannelEntity pc WHERE pc.profileId = :profileId" +
           "  )")
    List<AccountDTO> findAccountsByProfileIdAndProfileChannels(@Param("profileId") UUID profileId);
}
