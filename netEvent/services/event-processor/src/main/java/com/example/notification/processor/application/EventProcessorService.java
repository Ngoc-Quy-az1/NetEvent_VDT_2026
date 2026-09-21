package com.example.notification.processor.application;

import com.example.notification.common.kafka.NotificationTopics;
import com.example.notification.contract.approval.ApprovalRequestedEvent;
import com.example.notification.contract.approval.ApprovalResultEvent;
import com.example.notification.contract.audit.LifecycleEvent;
import com.example.notification.contract.profile.ProfileEvent;
import com.example.notification.processor.domain.context.NotificationContext;
import com.example.notification.processor.domain.rule.BusinessRuleResolver;
import com.example.notification.processor.infrastructure.query.KpiQueryExecutor;
import com.example.notification.processor.infrastructure.template.TemplateRenderer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@Slf4j
public class EventProcessorService {

    private final KpiQueryExecutor kpiQueryExecutor;
    private final BusinessRuleResolver businessRuleResolver;
    private final TemplateRenderer templateRenderer;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public EventProcessorService(KpiQueryExecutor kpiQueryExecutor,
                                 BusinessRuleResolver businessRuleResolver,
                                 TemplateRenderer templateRenderer,
                                 KafkaTemplate<String, Object> kafkaTemplate) {
        this.kpiQueryExecutor = kpiQueryExecutor;
        this.businessRuleResolver = businessRuleResolver;
        this.templateRenderer = templateRenderer;
        this.kafkaTemplate = kafkaTemplate;
    }

    public void processProfileEvent(ProfileEvent event) {
        UUID runId = UUID.randomUUID();
        UUID notificationId = UUID.randomUUID();
        UUID correlationId = event.getCorrelationId() != null ? event.getCorrelationId() : UUID.randomUUID();
        
        log.info("Processing ProfileEvent: profileId={}, correlationId={}, runId={}",
                event.getProfileId(), correlationId, runId);

        Map<String, Object> kpiData = kpiQueryExecutor.queryKpiContext(event.getProfileId(), event.getPayload());
        if (kpiData == null) kpiData = new HashMap<>();
        if (event.getPayload() != null) {
            kpiData.putAll(event.getPayload());
        }

        NotificationContext context = new NotificationContext(event.getProfileId(), correlationId, event.getEventType(), kpiData);

        boolean requireApproval = businessRuleResolver.evaluateApprovalRequirement(context);
        context.setRequireApproval(requireApproval);

        String title = templateRenderer.renderTitle(null, kpiData);
        String content = templateRenderer.renderContent(null, kpiData);
        context.setRenderedTitle(title);
        context.setRenderedContent(content);

        String checksum = NotificationTopics.calculateChecksum(content);
        UUID templateId = UUID.randomUUID();
        UUID draftId = UUID.randomUUID();

        if (requireApproval) {
            UUID approvalId = UUID.randomUUID();
            ApprovalRequestedEvent approvalEvent = new ApprovalRequestedEvent(
                    notificationId,
                    runId,
                    event.getProfileId(),
                    approvalId,
                    correlationId,
                    1, // level 1
                    title,
                    content,
                    templateId,
                    1, // version 1
                    checksum,
                    "APPROVER",
                    Instant.now(),
                    Instant.now().plusSeconds(86400) // 24h default timeout
            );
            log.info("Publishing ApprovalRequestedEvent to topic {}: approvalId={}, checksum={}",
                    NotificationTopics.APPROVAL_REQUESTED, approvalId, checksum);
            kafkaTemplate.send(NotificationTopics.APPROVAL_REQUESTED, approvalId.toString(), approvalEvent);

            LifecycleEvent lifecycleEvent = new LifecycleEvent(
                    correlationId, runId, "ApprovalRequested", "event-processor",
                    "WAITING_APPROVAL", "SUCCESS", "SYSTEM",
                    Map.of("approvalId", approvalId.toString(), "checksum", checksum), Instant.now()
            );
            kafkaTemplate.send(NotificationTopics.AUDIT_LIFECYCLE, correlationId.toString(), lifecycleEvent);
        } else {
            log.info("Approval not required. Auto-approving and sending to topic {}", NotificationTopics.NOTIFICATION_APPROVED);
            ApprovalResultEvent approvedEvent = new ApprovalResultEvent(
                    notificationId,
                    runId,
                    event.getProfileId(),
                    UUID.randomUUID(),
                    correlationId,
                    "APPROVED",
                    1,
                    draftId,
                    checksum,
                    content,
                    "SYSTEM_AUTO",
                    "No approval required for this profile",
                    Instant.now()
            );
            kafkaTemplate.send(NotificationTopics.NOTIFICATION_APPROVED, runId.toString(), approvedEvent);

            LifecycleEvent lifecycleEvent = new LifecycleEvent(
                    correlationId, runId, "AutoApproved", "event-processor",
                    "APPROVED", "SUCCESS", "SYSTEM",
                    Map.of("checksum", checksum), Instant.now()
            );
            kafkaTemplate.send(NotificationTopics.AUDIT_LIFECYCLE, correlationId.toString(), lifecycleEvent);
        }
    }
}
