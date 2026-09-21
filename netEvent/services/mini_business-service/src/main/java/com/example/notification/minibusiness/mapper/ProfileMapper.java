package com.example.notification.minibusiness.mapper;

import com.example.notification.minibusiness.domain.entity.ProfileEntity;
import com.example.notification.minibusiness.dto.CreateProfileRequest;
import com.example.notification.minibusiness.dto.ProfileResponse;
import com.example.notification.minibusiness.dto.UpdateProfileRequest;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class ProfileMapper {

    @Mapping(target = "profileId", ignore = true)
    @Mapping(target = "status", constant = "ACTIVE")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    public abstract ProfileEntity toEntity(CreateProfileRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "profileId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    public abstract void updateEntity(@MappingTarget ProfileEntity entity, UpdateProfileRequest request);

    public abstract ProfileResponse toResponse(ProfileEntity entity);

    public abstract List<ProfileResponse> toResponseList(List<ProfileEntity> entities);

    @AfterMapping
    protected void mapRelations(ProfileEntity entity, @MappingTarget ProfileResponse response) {
        if (entity == null || response == null) {
            return;
        }

        // Account mapping
        if (entity.getProfileAccounts() != null && !entity.getProfileAccounts().isEmpty()) {
            List<java.util.UUID> accountIds = entity.getProfileAccounts().stream()
                    .map(pa -> pa.getAccountId())
                    .collect(java.util.stream.Collectors.toList());
            response.setAccountId(accountIds);

            List<String> accountNames = entity.getProfileAccounts().stream()
                    .map(pa -> pa.getAccount() != null 
                        ? (pa.getAccount().getFullName() != null ? pa.getAccount().getFullName() : pa.getAccount().getUsername())
                        : null)
                    .filter(name -> name != null)
                    .collect(java.util.stream.Collectors.toList());
            response.setAccountName(accountNames);
        }

        if (entity.getProfileChannels() != null && !entity.getProfileChannels().isEmpty()) {
            List<java.util.UUID> channelIds = entity.getProfileChannels().stream()
                    .map(pc -> pc.getChannelId())
                    .collect(java.util.stream.Collectors.toList());
            response.setChannel_id(channelIds);

            List<String> channelNames = entity.getProfileChannels().stream()
                    .map(pc -> pc.getChannel() != null ? pc.getChannel().getChannelName() : null)
                    .filter(name -> name != null)
                    .collect(java.util.stream.Collectors.toList());
            response.setChannel_name(channelNames);
        }

        // Business Rule mapping
        if (entity.getProfileBusinessRules() != null && !entity.getProfileBusinessRules().isEmpty()) {
            List<java.util.UUID> ruleIds = entity.getProfileBusinessRules().stream()
                    .map(pbr -> pbr.getBusinessRuleId())
                    .collect(java.util.stream.Collectors.toList());
            response.setBusiness_rule_id(ruleIds);

            List<String> ruleNames = entity.getProfileBusinessRules().stream()
                    .map(pbr -> pbr.getBusinessRule() != null ? pbr.getBusinessRule().getBusinessRuleName() : null)
                    .filter(name -> name != null)
                    .collect(java.util.stream.Collectors.toList());
            response.setBusiness_rule_name(ruleNames);
        }
    }
}
