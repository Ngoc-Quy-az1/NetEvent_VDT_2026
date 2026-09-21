package com.example.notification.routing.infrastructure.kafka;

import com.example.notification.common.kafka.NotificationTopics;
import com.example.notification.contract.approval.ApprovalResultEvent;
import com.example.notification.routing.application.RoutingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class ApprovedEventConsumer {

    private final RoutingService routingService;

    @KafkaListener(topics = NotificationTopics.NOTIFICATION_APPROVED, groupId = "routing-service-group")
    public void consumeApprovedEvent(ApprovalResultEvent event) {
        log.info("Routing engine received APPROVED event for notificationId={}, runId={}, status={}",
                event.getNotificationId(), event.getRunId(), event.getStatus());
        routingService.processApprovedEvent(event);
    }
}
