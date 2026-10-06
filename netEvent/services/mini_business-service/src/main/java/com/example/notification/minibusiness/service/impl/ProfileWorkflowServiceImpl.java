package com.example.notification.minibusiness.service.impl;

import com.example.notification.minibusiness.domain.entity.AccountEntity;
import com.example.notification.minibusiness.domain.entity.BusinessRuleEntity;
import com.example.notification.minibusiness.dto.BusinessRuleCredentialResponse;
import com.example.notification.minibusiness.domain.entity.ChannelEntity;
import com.example.notification.minibusiness.domain.entity.EventEntity;
import com.example.notification.minibusiness.domain.entity.EventTypeEntity;
import com.example.notification.minibusiness.domain.entity.EventSessionEntity;
import com.example.notification.minibusiness.domain.entity.HolidayOccasionEntity;
import com.example.notification.minibusiness.domain.entity.ProfileAccountEntity;
import com.example.notification.minibusiness.domain.entity.ProfileChannelEntity;
import com.example.notification.minibusiness.domain.entity.ProfileEntity;
import com.example.notification.minibusiness.domain.entity.ProfileEventSessionEntity;
import com.example.notification.minibusiness.domain.entity.ProfileGroupEntity;
import com.example.notification.minibusiness.domain.entity.TemplateEntity;
import com.example.notification.minibusiness.dto.*;
import com.example.notification.minibusiness.mapper.AccountMapper;
import com.example.notification.minibusiness.mapper.BusinessRuleMapper;
import com.example.notification.minibusiness.mapper.EventMapper;
import com.example.notification.minibusiness.mapper.ProfileMapper;
import com.example.notification.minibusiness.mapper.TemplateMapper;
import com.example.notification.minibusiness.repository.*;
import com.example.notification.minibusiness.service.ProfileWorkflowService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.example.notification.minibusiness.domain.entity.ProfileBusinessRuleMappingEntity;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProfileWorkflowServiceImpl implements ProfileWorkflowService {

    private final ProfileRepository profileRepository;
    private final BusinessRuleRepository businessRuleRepository;
    private final ProfileBusinessRuleMappingRepository profileBusinessRuleMappingRepository;
    private final TemplateRepository templateRepository;
    private final ChannelRepository channelRepository;
    private final ProfileChannelRepository profileChannelRepository;
    private final AccountRepository accountRepository;
    private final ProfileAccountRepository profileAccountRepository;
    private final ProfileEventSessionRepository profileEventSessionRepository;
    private final EventSessionRepository eventSessionRepository;
    private final EventRepository eventRepository;
    private final EventTypeRepository eventTypeRepository;
    private final HolidayOccasionRepository holidayOccasionRepository;
    private final ProfileGroupRepository profileGroupRepository;

    private final ProfileMapper profileMapper;
    private final TemplateMapper templateMapper;
    private final BusinessRuleMapper businessRuleMapper;
    private final AccountMapper accountMapper;
    private final EventMapper eventMapper;

    // --- Profile CRUD Operations ---

    @Override
    @Transactional
    public ProfileResponse createProfile(CreateProfileRequest request) {
        log.info("Creating profile: {}", request.getProfileName());
        ProfileEntity entity = profileMapper.toEntity(request);
        ProfileEntity saved = profileRepository.save(entity);

        if (request.getAccountIds() != null && !request.getAccountIds().isEmpty()) {
            for (UUID accountId : request.getAccountIds()) {
                AccountEntity account = accountRepository.findById(accountId).orElse(null);
                if (account != null) {
                    ProfileAccountEntity pa = ProfileAccountEntity.builder()
                            .profileId(saved.getProfileId())
                            .profile(saved)
                            .accountId(accountId)
                            .account(account)
                            .build();
                    profileAccountRepository.save(pa);
                    saved.getProfileAccounts().add(pa);
                }
            }
        }

        syncProfileChannels(saved, request.getChannelIds(), request.getChannelTemplates());
        saveProfileGroups(saved.getProfileId(), request.getProfileGroups());

        return profileMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public ProfileResponse getProfile(UUID profileId) {
        log.info("Fetching profile details for ID: {}", profileId);
        return profileRepository.findById(profileId)
                .map(this::toProfileResponse)
                .orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProfileResponse> getAllProfiles() {
        log.info("Fetching all profiles with manual DTO assembly");
        List<ProfileEntity> profiles = profileRepository.findAll();
        List<ProfileResponse> responseList = new java.util.ArrayList<>();

        for (ProfileEntity profile : profiles) {
            ProfileResponse response = toProfileResponse(profile);

            // 1. Account mapping
            List<ProfileAccountEntity> accountMappings = profileAccountRepository.findByProfileId(profile.getProfileId());
            if (!accountMappings.isEmpty()) {
                List<UUID> accountIds = accountMappings.stream()
                        .map(ProfileAccountEntity::getAccountId)
                        .collect(Collectors.toList());
                response.setAccountId(accountIds);

                List<String> accountNames = accountMappings.stream()
                        .map(pa -> pa.getAccount() != null 
                                ? (pa.getAccount().getFullName() != null ? pa.getAccount().getFullName() : pa.getAccount().getUsername())
                                : null)
                        .filter(name -> name != null)
                        .collect(Collectors.toList());
                response.setAccountName(accountNames);
            }

            // 2. Channel mapping
            List<ProfileChannelEntity> channelMappings = profileChannelRepository.findByProfileId(profile.getProfileId());
            if (!channelMappings.isEmpty()) {
                List<UUID> channelIds = channelMappings.stream()
                        .map(ProfileChannelEntity::getChannelId)
                        .collect(Collectors.toList());
                response.setChannel_id(channelIds);

                List<String> channelNames = channelMappings.stream()
                        .map(pc -> pc.getChannel() != null ? pc.getChannel().getChannelName() : null)
                        .filter(name -> name != null)
                        .collect(Collectors.toList());
                response.setChannel_name(channelNames);
            }

            // 3. Business Rule mapping
            List<ProfileBusinessRuleMappingEntity> ruleMappings = profileBusinessRuleMappingRepository.findByProfileId(profile.getProfileId());
            if (!ruleMappings.isEmpty()) {
                List<UUID> ruleIds = ruleMappings.stream()
                        .map(ProfileBusinessRuleMappingEntity::getBusinessRuleId)
                        .collect(Collectors.toList());
                response.setBusiness_rule_id(ruleIds);

                List<String> ruleNames = ruleMappings.stream()
                        .map(pbr -> pbr.getBusinessRule() != null ? pbr.getBusinessRule().getBusinessRuleName() : null)
                        .filter(name -> name != null)
                        .collect(Collectors.toList());
                response.setBusiness_rule_name(ruleNames);
            }

            // 4. Event session mapping
            List<ProfileEventSessionEntity> sessionMappings = profileEventSessionRepository.findByProfileId(profile.getProfileId());
            if (!sessionMappings.isEmpty()) {
                response.setSessionIds(sessionMappings.stream()
                        .map(ProfileEventSessionEntity::getSessionId)
                        .collect(Collectors.toList()));
                response.setSession_event_code(sessionMappings.stream()
                        .map(mapping -> mapping.getEventSession() != null ? mapping.getEventSession().getSessionEventCode() : null)
                        .filter(java.util.Objects::nonNull)
                        .collect(Collectors.toList()));
            }

            responseList.add(response);
        }

        return responseList;
    }

    @Override
    @Transactional
    public ProfileResponse updateProfile(UUID profileId, UpdateProfileRequest request) {
        ProfileEntity entity = profileRepository.findById(profileId)
                .orElseThrow(() -> new IllegalArgumentException("Profile not found with ID: " + profileId));

        profileMapper.updateEntity(entity, request);

        if (request.getAccountIds() != null) {
            entity.getProfileAccounts().clear();
            for (UUID accountId : request.getAccountIds()) {
                AccountEntity account = accountRepository.findById(accountId).orElse(null);
                if (account != null) {
                    ProfileAccountEntity pa = ProfileAccountEntity.builder()
                            .profileId(profileId)
                            .profile(entity)
                            .accountId(accountId)
                            .account(account)
                            .build();
                    entity.getProfileAccounts().add(pa);
                }
            }
        }

        if (request.getChannelIds() != null || request.getChannels() != null || request.getChannelTemplates() != null) {
            java.util.Set<UUID> desiredChannelIds = new java.util.LinkedHashSet<>();

            if (request.getChannelIds() != null) {
                desiredChannelIds.addAll(request.getChannelIds());
            }

            if (request.getChannels() != null) {
                for (String channelName : request.getChannels()) {
                    ChannelEntity channel = channelRepository.findByChannelName(channelName)
                            .orElseGet(() -> channelRepository.save(ChannelEntity.builder().channelName(channelName).build()));
                    desiredChannelIds.add(channel.getChannelId());
                }
            }

            if (request.getChannelTemplates() != null) {
                desiredChannelIds.addAll(request.getChannelTemplates().stream()
                        .map(ProfileChannelTemplateRequest::getChannelId)
                        .filter(java.util.Objects::nonNull).collect(Collectors.toList()));
            }

            // Never clear then recreate the same composite key in one persistence context.
            // Hibernate would regard it as a deleted entity being re-saved.
            entity.getProfileChannels().removeIf(pc -> !desiredChannelIds.contains(pc.getChannelId()));
            java.util.Set<UUID> currentChannelIds = entity.getProfileChannels().stream()
                    .map(ProfileChannelEntity::getChannelId).collect(Collectors.toSet());
            for (UUID channelId : desiredChannelIds) {
                if (currentChannelIds.contains(channelId)) continue;
                ChannelEntity channel = channelRepository.findById(channelId).orElse(null);
                if (channel != null) {
                    entity.getProfileChannels().add(ProfileChannelEntity.builder()
                            .profileId(profileId).profile(entity).channelId(channelId).channel(channel).build());
                }
            }
            if (request.getChannelTemplates() != null) {
                java.util.Map<UUID, UUID> templatesByChannel = channelTemplateIds(request.getChannelTemplates());
                entity.getProfileChannels().forEach(pc -> pc.setTemplateId(templatesByChannel.get(pc.getChannelId())));
            }
        }

        if (request.getSessionIds() != null) {
            List<UUID> requestedSessionIds = request.getSessionIds().stream().distinct().collect(Collectors.toList());
            List<EventSessionEntity> sessions = eventSessionRepository.findAllById(requestedSessionIds);
            if (sessions.size() != requestedSessionIds.size()) {
                throw new IllegalArgumentException("One or more event sessions do not exist");
            }

            if (sessions.stream().map(EventSessionEntity::getEventId).distinct().count() > 1) {
                throw new IllegalArgumentException("A profile can only be associated with sessions from one event");
            }

            profileEventSessionRepository.deleteByProfileId(profileId);
            profileEventSessionRepository.saveAll(sessions.stream()
                    .map(session -> ProfileEventSessionEntity.builder()
                            .profileId(profileId)
                            .sessionId(session.getSessionId())
                            .profile(entity)
                            .eventSession(session)
                            .build())
                    .collect(Collectors.toList()));
        }
        if (request.getProfileGroups() != null) saveProfileGroups(profileId, request.getProfileGroups());

        ProfileEntity updated = profileRepository.save(entity);
        return toProfileResponse(updated);
    }

    private void saveProfileGroups(UUID profileId, List<ProfileGroupRequest> groups) {
        if (groups == null) return;
        profileGroupRepository.deleteByProfileId(profileId);
        profileGroupRepository.saveAll(groups.stream().filter(g -> g.getGroupId() != null && g.getChannelId() != null)
                .map(g -> ProfileGroupEntity.builder().profileId(profileId).groupId(g.getGroupId()).channelId(g.getChannelId()).groupConfig(g.getGroupConfig()).build()).collect(Collectors.toList()));
    }

    private void syncProfileChannels(ProfileEntity profile, List<UUID> channelIds,
                                     List<ProfileChannelTemplateRequest> channelTemplates) {
        java.util.Map<UUID, UUID> templatesByChannel = channelTemplateIds(channelTemplates);
        java.util.Set<UUID> desired = new java.util.LinkedHashSet<>();
        if (channelIds != null) desired.addAll(channelIds);
        desired.addAll(templatesByChannel.keySet());
        for (UUID channelId : desired) {
            ChannelEntity channel = channelRepository.findById(channelId)
                    .orElseThrow(() -> new IllegalArgumentException("Channel not found: " + channelId));
            profile.getProfileChannels().add(ProfileChannelEntity.builder()
                    .profileId(profile.getProfileId()).profile(profile).channelId(channelId).channel(channel)
                    .templateId(templatesByChannel.get(channelId)).build());
        }
    }

    private java.util.Map<UUID, UUID> channelTemplateIds(List<ProfileChannelTemplateRequest> requests) {
        java.util.Map<UUID, UUID> result = new java.util.LinkedHashMap<>();
        if (requests == null) return result;
        for (ProfileChannelTemplateRequest request : requests) {
            if (request.getChannelId() == null || request.getTemplateId() == null) {
                throw new IllegalArgumentException("Each channel template configuration requires channelId and templateId");
            }
            TemplateEntity template = templateRepository.findById(request.getTemplateId())
                    .orElseThrow(() -> new IllegalArgumentException("Template not found: " + request.getTemplateId()));
            if (!request.getChannelId().equals(template.getChannelId())) {
                throw new IllegalArgumentException("Selected template does not belong to its channel");
            }
            if (result.put(request.getChannelId(), request.getTemplateId()) != null) {
                throw new IllegalArgumentException("Only one template can be selected per channel");
            }
        }
        return result;
    }

    private ProfileResponse toProfileResponse(ProfileEntity profile) {
        ProfileResponse response = profileMapper.toResponse(profile);
        List<ProfileEventSessionEntity> sessionMappings = profileEventSessionRepository.findByProfileId(profile.getProfileId());
        if (!sessionMappings.isEmpty()) {
            response.setSessionIds(sessionMappings.stream()
                    .map(ProfileEventSessionEntity::getSessionId)
                    .collect(Collectors.toList()));
            response.setSession_event_code(sessionMappings.stream()
                    .map(mapping -> mapping.getEventSession() != null ? mapping.getEventSession().getSessionEventCode() : null)
                    .filter(java.util.Objects::nonNull)
                    .collect(Collectors.toList()));
        }
        response.setProfileGroups(profileGroupRepository.findByProfileId(profile.getProfileId()).stream()
                .map(g -> ProfileGroupResponse.builder().groupId(g.getGroupId()).channelId(g.getChannelId()).groupConfig(g.getGroupConfig()).build()).collect(Collectors.toList()));
        response.setChannelTemplates(profileChannelRepository.findByProfileId(profile.getProfileId()).stream()
                .map(pc -> ProfileChannelTemplateResponse.builder().channelId(pc.getChannelId())
                        .channelName(pc.getChannel() == null ? null : pc.getChannel().getChannelName())
                        .templateId(pc.getTemplateId())
                        .templateName(pc.getTemplate() == null ? null : pc.getTemplate().getTemplateName()).build())
                .collect(Collectors.toList()));
        return response;
    }

    @Override
    @Transactional
    public void deleteProfile(UUID profileId) {
        log.info("Deleting profile ID: {}", profileId);
        if (!profileRepository.existsById(profileId)) {
            throw new IllegalArgumentException("Profile not found with ID: " + profileId);
        }
        profileRepository.deleteById(profileId);
    }

    // --- Template CRUD Operations ---

    @Override
    @Transactional
    public TemplateResponse configureTemplate(ConfigureTemplateRequest request) {
        log.info("Configuring template: {}", request.getTemplateName());
        TemplateEntity entity = templateMapper.toEntity(request);
        TemplateEntity saved = templateRepository.save(entity);
        return templateMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public TemplateResponse getTemplate(UUID templateId) {
        log.info("Fetching template for ID: {}", templateId);
        return templateRepository.findById(templateId)
                .map(templateMapper::toResponse)
                .orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TemplateResponse> getAllTemplates() {
        log.info("Fetching all templates");
        List<TemplateEntity> templates = templateRepository.findAll();
        return templateMapper.toResponseList(templates);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChannelResponse> getAllChannels() {
        log.info("Fetching all channels");
        List<ChannelEntity> channels = channelRepository.findAll();
        return channels.stream()
                .map(c -> ChannelResponse.builder()
                        .channelId(c.getChannelId())
                        .channelName(c.getChannelName())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public TemplateResponse updateTemplate(UUID templateId, ConfigureTemplateRequest request) {
        log.info("Updating template ID: {}", templateId);
        TemplateEntity entity = templateRepository.findById(templateId)
                .orElseThrow(() -> new IllegalArgumentException("Template not found with ID: " + templateId));

        templateMapper.updateEntity(entity, request);
        TemplateEntity updated = templateRepository.save(entity);
        return templateMapper.toResponse(updated);
    }

    @Override
    @Transactional
    public void deleteTemplate(UUID templateId) {
        log.info("Deleting template ID: {}", templateId);
        if (!templateRepository.existsById(templateId)) {
            throw new IllegalArgumentException("Template not found with ID: " + templateId);
        }
        templateRepository.deleteById(templateId);
    }

    // --- Business Rule Operations ---

    @Override
    @Transactional
    public void configureProfileRules(UUID profileId, ConfigureProfileRuleRequest request) {
        log.info("Configuring rules for profile ID: {}", profileId);
        ProfileEntity profile = profileRepository.findById(profileId)
                .orElseThrow(() -> new IllegalArgumentException("Profile not found with ID: " + profileId));

        profile.getProfileBusinessRules().clear();

        if (request != null && request.getRules() != null) {
            for (ConfigureProfileRuleRequest.RuleMappingItem item : request.getRules()) {
                BusinessRuleEntity rule = businessRuleRepository.findById(item.getBusinessRuleId())
                        .orElseThrow(() -> new IllegalArgumentException("Business rule not found with ID: " + item.getBusinessRuleId()));

                ProfileBusinessRuleMappingEntity mapping = ProfileBusinessRuleMappingEntity.builder()
                        .profileId(profileId)
                        .profile(profile)
                        .businessRuleId(rule.getBusinessRuleId())
                        .businessRule(rule)
                        .status(item.getStatus() != null ? item.getStatus() : "ACTIVE")
                        .build();

                profile.getProfileBusinessRules().add(mapping);
            }
        }
        profileRepository.save(profile);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BusinessRuleResponse> getAllBusinessRules() {
        log.info("Fetching all business rules");
        List<BusinessRuleEntity> rules = businessRuleRepository.findAll();
        return businessRuleMapper.toResponseList(rules);
    }   

    @Override
    @Transactional(readOnly = true)
    public BusinessRuleResponse getBusinessRule(UUID ruleId) {
        log.info("Fetching business rule by ID: {}", ruleId);
        return businessRuleRepository.findById(ruleId)
                .map(businessRuleMapper::toResponse)
                .orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public BusinessRuleCredentialResponse getBusinessRuleCredential(UUID ruleId) {
        BusinessRuleEntity rule = businessRuleRepository.findById(ruleId)
                .orElseThrow(() -> new IllegalArgumentException("Business rule not found with ID: " + ruleId));
        return new BusinessRuleCredentialResponse(rule.getSourceUsername(), rule.getSourcePassword());
    }

    @Override
    @Transactional(readOnly = true)
    public List<BusinessRuleResponse> getRulesByProfileId(UUID profileId) {
        log.info("Fetching business rules mapped to profile ID: {}", profileId);
        List<ProfileBusinessRuleMappingEntity> mappings = profileBusinessRuleMappingRepository.findByProfileId(profileId);
        List<BusinessRuleEntity> rules = mappings.stream()
                .map(ProfileBusinessRuleMappingEntity::getBusinessRule)
                .collect(Collectors.toList());
        return businessRuleMapper.toResponseList(rules);
    }

    @Override
    @Transactional
    public BusinessRuleResponse createBusinessRule(BusinessRuleResponse request) {
        log.info("Creating business rule: {}", request.getBusinessRuleCode());
        String[] sqlQueries = request.getSqlQueries() == null ? new String[0] : request.getSqlQueries();
        String[] summarySqlQueries = request.getSummarySqlQueries() == null ? new String[0] : request.getSummarySqlQueries();
        BusinessRuleEntity entity = BusinessRuleEntity.builder()
                .businessRuleCode(request.getBusinessRuleCode())
                .businessRuleName(request.getBusinessRuleName())
                .sourceDbType(request.getSourceDbType())
                .sourceHost(request.getSourceHost())
                .sourcePort(request.getSourcePort())
                .sourceDatabase(request.getSourceDatabase())
                .sourceSchema(request.getSourceSchema())
                .sourceTable(request.getSourceTable())
                .sourceConnectionRef(request.getSourceConnectionRef())
                .sourceUsername(request.getSourceUsername())
                .sourcePassword(passwordForStorage(request))
                .sqlQueries(sqlQueries)
                .summarySqlQueries(summarySqlQueries)
                .description(request.getDescription())
                .status(request.getStatus() != null ? request.getStatus() : "ACTIVE")
                .build();
        BusinessRuleEntity saved = businessRuleRepository.save(entity);
        return businessRuleMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public BusinessRuleResponse updateBusinessRule(UUID ruleId, BusinessRuleResponse request) {
        log.info("Updating business rule ID: {}", ruleId);
        BusinessRuleEntity entity = businessRuleRepository.findById(ruleId)
                .orElseThrow(() -> new IllegalArgumentException("Business rule not found with ID: " + ruleId));
        if (request.getBusinessRuleName() != null) entity.setBusinessRuleName(request.getBusinessRuleName());
        if (request.getSourceDbType() != null) entity.setSourceDbType(request.getSourceDbType());
        if (request.getSourceHost() != null) entity.setSourceHost(request.getSourceHost());
        if (request.getSourcePort() != null) entity.setSourcePort(request.getSourcePort());
        if (request.getSourceDatabase() != null) entity.setSourceDatabase(request.getSourceDatabase());
        if (request.getSourceSchema() != null) entity.setSourceSchema(request.getSourceSchema());
        if (request.getSourceTable() != null) entity.setSourceTable(request.getSourceTable());
        if (request.getSourceConnectionRef() != null) entity.setSourceConnectionRef(request.getSourceConnectionRef());
        if (request.getSourceUsername() != null) entity.setSourceUsername(request.getSourceUsername());
        if (request.getSourcePassword() != null && !request.getSourcePassword().isBlank()) {
            entity.setSourcePassword(request.getSourcePassword());
        }
        if (request.getSqlQueries() != null) entity.setSqlQueries(request.getSqlQueries());
        if (request.getSummarySqlQueries() != null) entity.setSummarySqlQueries(request.getSummarySqlQueries());
        if (request.getDescription() != null) entity.setDescription(request.getDescription());
        if (request.getStatus() != null) entity.setStatus(request.getStatus());
        BusinessRuleEntity saved = businessRuleRepository.save(entity);
        return businessRuleMapper.toResponse(saved);
    }

    private String passwordForStorage(BusinessRuleResponse request) {
        if (request.getSourcePassword() == null || request.getSourcePassword().isBlank()) {
            throw new IllegalArgumentException("sourcePassword is required when creating a business rule");
        }
        return request.getSourcePassword();
    }


    @Override
    @Transactional
    public void deleteBusinessRule(UUID ruleId) {
        log.info("Deleting business rule ID: {}", ruleId);
        businessRuleRepository.deleteById(ruleId);
    }

    // --- Account Operations ---

    @Override
    @Transactional(readOnly = true)
    public List<AccountResponse> getAllAccounts() {
        log.info("Fetching all accounts");
        List<AccountEntity> accounts = accountRepository.findAll();
        return accountMapper.toResponseList(accounts);
    }

    @Override
    @Transactional(readOnly = true)
    public AccountResponse getAccount(UUID accountId) {
        log.info("Fetching account by ID: {}", accountId);
        return accountRepository.findById(accountId)
                .map(accountMapper::toResponse)
                .orElse(null);
    }

    @Override
    @Transactional
    public AccountResponse createAccount(AccountResponse request) {
        log.info("Creating account: {}", request.getUsername());
        AccountEntity entity = AccountEntity.builder()
                .username(request.getUsername())
                .fullName(request.getFullName())
                .email(request.getEmail())
                .cellPhone(request.getCellPhone())
                .areaCode(request.getAreaCode())
                .language(request.getLanguage())
                .roleId(request.getRoleId())
                .build();
        AccountEntity saved = accountRepository.save(entity);
        return accountMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public AccountResponse updateAccount(UUID accountId, AccountResponse request) {
        log.info("Updating account ID: {}", accountId);
        AccountEntity entity = accountRepository.findById(accountId)
                .orElseThrow(() -> new IllegalArgumentException("Account not found with ID: " + accountId));
        if (request.getFullName() != null) entity.setFullName(request.getFullName());
        if (request.getEmail() != null) entity.setEmail(request.getEmail());
        if (request.getCellPhone() != null) entity.setCellPhone(request.getCellPhone());
        if (request.getAreaCode() != null) entity.setAreaCode(request.getAreaCode());
        if (request.getLanguage() != null) entity.setLanguage(request.getLanguage());
        if (request.getRoleId() != null) entity.setRoleId(request.getRoleId());
        AccountEntity saved = accountRepository.save(entity);
        return accountMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public void deleteAccount(UUID accountId) {
        log.info("Deleting account ID: {}", accountId);
        accountRepository.deleteById(accountId);
    }

    // --- Event Operations ---

    @Override
    @Transactional(readOnly = true)
    public List<EventResponse> getAllEvents() {
        log.info("Fetching all events");
        List<EventEntity> events = eventRepository.findAll();
        return eventMapper.toResponseList(events);
    }

    @Override
    @Transactional(readOnly = true)
    public EventResponse getEvent(UUID eventId) {
        log.info("Fetching event by ID: {}", eventId);
        return eventRepository.findById(eventId)
                .map(eventMapper::toResponse)
                .orElse(null);
    }

    @Override
    @Transactional
    public EventResponse createEvent(CreateEventRequest request) {
        log.info("Creating event: {}", request.getEventCode());
        EventEntity entity = EventEntity.builder()
                .eventCode(request.getEventCode())
                .eventName(request.getEventName())
                .eventTypeId(resolveEventTypeId(request))
                .holidayId(resolveHolidayId(request))
                .eventLevel(request.getEventLevel())
                .annual(request.getAnnual() != null ? request.getAnnual() : false)
                .isLunar(request.getIsLunar() != null ? request.getIsLunar() : false)
                .status(request.getStatus() != null ? request.getStatus() : "ACTIVE")
                .build();
        EventEntity saved = eventRepository.save(entity);
        return eventMapper.toResponse(saved);
    }

    private UUID resolveEventTypeId(CreateEventRequest request) {
        if (request.getEventType() != null && request.getEventTypeId() != null) {
            throw new IllegalArgumentException("Provide either eventTypeId or eventType, not both");
        }
        if (request.getEventType() != null) {
            return eventTypeRepository.findByEventTypeCode(request.getEventType().getEventTypeCode())
                    .map(EventTypeEntity::getEventTypeId)
                    .orElseGet(() -> eventTypeRepository.save(EventTypeEntity.builder()
                            .eventTypeCode(request.getEventType().getEventTypeCode())
                            .eventTypeName(request.getEventType().getEventTypeName())
                            .build()).getEventTypeId());
        }
        if (request.getEventTypeId() != null && !eventTypeRepository.existsById(request.getEventTypeId())) {
            throw new IllegalArgumentException("Event type not found with ID: " + request.getEventTypeId());
        }
        return request.getEventTypeId();
    }

    private UUID resolveHolidayId(CreateEventRequest request) {
        if (request.getHoliday() != null && request.getHolidayId() != null) {
            throw new IllegalArgumentException("Provide either holidayId or holiday, not both");
        }
        if (request.getHoliday() != null) {
            return holidayOccasionRepository.findByHolidayCode(request.getHoliday().getHolidayCode())
                    .map(HolidayOccasionEntity::getHolidayId)
                    .orElseGet(() -> holidayOccasionRepository.save(HolidayOccasionEntity.builder()
                            .holidayCode(request.getHoliday().getHolidayCode())
                            .holidayName(request.getHoliday().getHolidayName())
                            .build()).getHolidayId());
        }
        if (request.getHolidayId() != null && !holidayOccasionRepository.existsById(request.getHolidayId())) {
            throw new IllegalArgumentException("Holiday not found with ID: " + request.getHolidayId());
        }
        return request.getHolidayId();
    }

    @Override
    @Transactional
    public EventResponse updateEvent(UUID eventId, EventResponse request) {
        log.info("Updating event ID: {}", eventId);
        EventEntity entity = eventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Event not found with ID: " + eventId));
        if (request.getEventName() != null) entity.setEventName(request.getEventName());
        if (request.getEventLevel() != null) entity.setEventLevel(request.getEventLevel());
        if (request.getAnnual() != null) entity.setAnnual(request.getAnnual());
        if (request.getIsLunar() != null) entity.setIsLunar(request.getIsLunar());
        if (request.getStatus() != null) entity.setStatus(request.getStatus());
        EventEntity saved = eventRepository.save(entity);
        return eventMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public void deleteEvent(UUID eventId) {
        log.info("Deleting event ID: {}", eventId);
        eventRepository.deleteById(eventId);
    }
}
