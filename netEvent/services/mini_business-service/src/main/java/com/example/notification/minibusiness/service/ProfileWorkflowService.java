package com.example.notification.minibusiness.service;

import com.example.notification.minibusiness.dto.*;

import java.util.List;
import java.util.UUID;

public interface ProfileWorkflowService {

    // --- Profile Operations ---
    ProfileResponse createProfile(CreateProfileRequest request);
    ProfileResponse getProfile(UUID profileId);
    List<ProfileResponse> getAllProfiles();
    ProfileResponse updateProfile(UUID profileId, UpdateProfileRequest request);
    void deleteProfile(UUID profileId);

    // --- Template Operations ---
    TemplateResponse configureTemplate(ConfigureTemplateRequest request);
    TemplateResponse getTemplate(UUID templateId);
    List<TemplateResponse> getAllTemplates();
    TemplateResponse updateTemplate(UUID templateId, ConfigureTemplateRequest request);
    void deleteTemplate(UUID templateId);

    // --- Channel Operations ---
    List<ChannelResponse> getAllChannels();

    // --- Business Rule Operations ---
    void configureProfileRules(UUID profileId, ConfigureProfileRuleRequest request);
    List<BusinessRuleResponse> getAllBusinessRules();
    BusinessRuleResponse getBusinessRule(UUID ruleId);
    List<BusinessRuleResponse> getRulesByProfileId(UUID profileId);
    BusinessRuleResponse createBusinessRule(BusinessRuleResponse request);
    BusinessRuleResponse updateBusinessRule(UUID ruleId, BusinessRuleResponse request);
    void deleteBusinessRule(UUID ruleId);

    // --- Account Operations ---
    List<AccountResponse> getAllAccounts();
    AccountResponse getAccount(UUID accountId);
    AccountResponse createAccount(AccountResponse request);
    AccountResponse updateAccount(UUID accountId, AccountResponse request);
    void deleteAccount(UUID accountId);

    // --- Event Operations ---
    List<EventResponse> getAllEvents();
    EventResponse getEvent(UUID eventId);
    EventResponse createEvent(EventResponse request);
    EventResponse updateEvent(UUID eventId, EventResponse request);
    void deleteEvent(UUID eventId);
}
