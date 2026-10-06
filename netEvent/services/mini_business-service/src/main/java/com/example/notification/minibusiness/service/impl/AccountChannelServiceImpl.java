package com.example.notification.minibusiness.service.impl;

import com.example.notification.minibusiness.domain.entity.AccountChannelEntity;
import com.example.notification.minibusiness.domain.entity.AccountEntity;
import com.example.notification.minibusiness.domain.entity.ChannelEntity;
import com.example.notification.minibusiness.dto.AccountChannelDto;
import com.example.notification.minibusiness.repository.AccountChannelRepository;
import com.example.notification.minibusiness.repository.AccountRepository;
import com.example.notification.minibusiness.repository.ChannelRepository;
import com.example.notification.minibusiness.service.AccountChannelService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountChannelServiceImpl implements AccountChannelService {

    private final AccountChannelRepository accountChannelRepository;
    private final AccountRepository accountRepository;
    private final ChannelRepository channelRepository;

    @Override
    @Transactional(readOnly = true)
    public List<AccountChannelDto> getAccountChannels(UUID accountId) {
        log.info("Fetching account channels - filter accountId: {}", accountId);

        List<AccountChannelEntity> entities;
        if (accountId != null) {
            entities = accountChannelRepository.findByAccountId(accountId);
        } else {
            entities = accountChannelRepository.findAll();
        }

        if (entities.isEmpty()) {
            return Collections.emptyList();
        }

        // Map danh sách sang DTO kèm thông tin Account và Channel
        Map<UUID, AccountEntity> accountMap = accountRepository.findAll().stream()
                .collect(Collectors.toMap(AccountEntity::getAccountId, a -> a, (a1, a2) -> a1));
        Map<UUID, ChannelEntity> channelMap = channelRepository.findAll().stream()
                .collect(Collectors.toMap(ChannelEntity::getChannelId, c -> c, (c1, c2) -> c1));

        return entities.stream()
                .map(e -> mapToDto(e, accountMap.get(e.getAccountId()), channelMap.get(e.getChannelId())))
                .sorted(Comparator.comparing(AccountChannelDto::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public AccountChannelDto getAccountChannelById(UUID id) {
        AccountChannelEntity entity = accountChannelRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("AccountChannel not found with ID: " + id));

        AccountEntity acc = entity.getAccountId() != null ? accountRepository.findById(entity.getAccountId()).orElse(null) : null;
        ChannelEntity chan = entity.getChannelId() != null ? channelRepository.findById(entity.getChannelId()).orElse(null) : null;

        return mapToDto(entity, acc, chan);
    }

    @Override
    @Transactional
    public AccountChannelDto createAccountChannel(AccountChannelDto request) {
        log.info("Creating new account channel for account: {}", request.getAccountId());

        AccountChannelEntity entity = AccountChannelEntity.builder()
                .accountId(request.getAccountId())
                .channelId(request.getChannelId())
                .contactValue(request.getContactValue())
                .label(request.getLabel() != null ? request.getLabel() : "Kênh liên hệ")
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .config(request.getConfig() != null ? request.getConfig() : "{}")
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        AccountChannelEntity saved = accountChannelRepository.save(entity);

        AccountEntity acc = entity.getAccountId() != null ? accountRepository.findById(entity.getAccountId()).orElse(null) : null;
        ChannelEntity chan = entity.getChannelId() != null ? channelRepository.findById(entity.getChannelId()).orElse(null) : null;

        return mapToDto(saved, acc, chan);
    }

    @Override
    @Transactional
    public AccountChannelDto updateAccountChannel(UUID id, AccountChannelDto request) {
        log.info("Updating account channel ID: {}", id);

        AccountChannelEntity entity = accountChannelRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("AccountChannel not found with ID: " + id));

        if (request.getAccountId() != null) entity.setAccountId(request.getAccountId());
        if (request.getChannelId() != null) entity.setChannelId(request.getChannelId());
        if (request.getContactValue() != null) entity.setContactValue(request.getContactValue());
        if (request.getLabel() != null) entity.setLabel(request.getLabel());
        if (request.getIsActive() != null) entity.setIsActive(request.getIsActive());
        if (request.getConfig() != null) entity.setConfig(request.getConfig());
        entity.setUpdatedAt(Instant.now());

        AccountChannelEntity saved = accountChannelRepository.save(entity);

        AccountEntity acc = entity.getAccountId() != null ? accountRepository.findById(entity.getAccountId()).orElse(null) : null;
        ChannelEntity chan = entity.getChannelId() != null ? channelRepository.findById(entity.getChannelId()).orElse(null) : null;

        return mapToDto(saved, acc, chan);
    }

    @Override
    @Transactional
    public void deleteAccountChannel(UUID id) {
        log.info("Deleting account channel ID: {}", id);
        accountChannelRepository.deleteById(id);
    }

    private AccountChannelDto mapToDto(AccountChannelEntity entity, AccountEntity acc, ChannelEntity chan) {
        return AccountChannelDto.builder()
                .accountChannelId(entity.getAccountChannelId())
                .accountId(entity.getAccountId())
                .accountUsername(acc != null ? acc.getUsername() : "")
                .accountFullName(acc != null ? acc.getFullName() : "")
                .channelId(entity.getChannelId())
                .channelName(chan != null ? chan.getChannelName() : "N/A")
                .contactValue(entity.getContactValue())
                .label(entity.getLabel())
                .isActive(entity.getIsActive() != null ? entity.getIsActive() : true)
                .config(entity.getConfig())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
