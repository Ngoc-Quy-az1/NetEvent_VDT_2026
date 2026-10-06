package com.example.notification.minibusiness.service;

import com.example.notification.minibusiness.dto.AccountChannelDto;

import java.util.List;
import java.util.UUID;

public interface AccountChannelService {
    List<AccountChannelDto> getAccountChannels(UUID accountId);
    AccountChannelDto getAccountChannelById(UUID id);
    AccountChannelDto createAccountChannel(AccountChannelDto request);
    AccountChannelDto updateAccountChannel(UUID id, AccountChannelDto request);
    void deleteAccountChannel(UUID id);
}
