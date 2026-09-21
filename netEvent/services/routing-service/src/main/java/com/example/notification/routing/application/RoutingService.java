package com.example.notification.routing.application;

import com.example.notification.common.kafka.NotificationTopics;
import com.example.notification.contract.approval.ApprovalResultEvent;
import com.example.notification.contract.audit.LifecycleEvent;
import com.example.notification.contract.routing.RoutingResultEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class RoutingService {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void processApprovedEvent(ApprovalResultEvent event) {
        log.info("Processing routing for notificationId={}, runId={}, correlationId={}",
                event.getNotificationId(), event.getRunId(), event.getCorrelationId());

        String content = event.getRenderedContent() != null ? event.getRenderedContent() : "Báo cáo cảnh báo KPI đã được phê duyệt.";
        String checksum = event.getChecksum() != null ? event.getChecksum() : NotificationTopics.calculateChecksum(content);

        // Verify checksum integrity
        String calculatedChecksum = NotificationTopics.calculateChecksum(content);
        if (event.getChecksum() != null && !event.getChecksum().isEmpty() && !event.getChecksum().equalsIgnoreCase(calculatedChecksum)) {
            log.error("Checksum mismatch during routing! Expected={}, Calculated={}", event.getChecksum(), calculatedChecksum);
        }

        // Default channels & recipient routing
        List<String> channels = List.of("EMAIL", "SMS", "OTT");
        
        // Mock recipient resolution & Mute Policy logic
        // In full production, this reads profile.allow_mute and recipient.status == 'MUTED'
        boolean allowMute = true; // default policy snapshot
        
        List<RoutingResultEvent.RouteItem> routes = new ArrayList<>();
        
        // Recipient 1: Active NOC Staff (Email + SMS + OTT)
        routes.add(new RoutingResultEvent.RouteItem("EMAIL", "REC_NOC_01", "noc-alerts@viettel.com.vn", content, checksum));
        routes.add(new RoutingResultEvent.RouteItem("SMS", "REC_NOC_01", "+84988888888", content, checksum));
        routes.add(new RoutingResultEvent.RouteItem("OTT", "REC_NOC_01", "@noc_telegram_bot", content, checksum));

        // Recipient 2: Operations Manager
        routes.add(new RoutingResultEvent.RouteItem("EMAIL", "REC_MGR_02", "manager-ops@viettel.com.vn", content, checksum));

        // Recipient 3: Muted Staff (if allowMute == false, force send; if allowMute == true, skip)
        if (!allowMute) {
            routes.add(new RoutingResultEvent.RouteItem("SMS", "REC_MUTED_03", "+84977777777", content, checksum));
        } else {
            log.info("Skipping muted recipient REC_MUTED_03 because allowMute is true");
        }

        List<String> recipientIds = List.of("REC_NOC_01", "REC_MGR_02");

        RoutingResultEvent routedEvent = new RoutingResultEvent(
                event.getNotificationId(),
                event.getRunId(),
                event.getProfileId(),
                event.getCorrelationId(),
                "Thông báo cảnh báo mạng [Đã duyệt]",
                content,
                checksum,
                channels,
                recipientIds,
                routes,
                Instant.now()
        );

        log.info("Publishing RoutingResultEvent to topic {}: notificationId={}, totalRoutes={}",
                NotificationTopics.NOTIFICATION_ROUTED, event.getNotificationId(), routes.size());
        kafkaTemplate.send(NotificationTopics.NOTIFICATION_ROUTED,
                event.getRunId() != null ? event.getRunId().toString() : event.getNotificationId().toString(),
                routedEvent);

        LifecycleEvent lifecycleEvent = new LifecycleEvent(
                event.getCorrelationId(), event.getRunId(), "NotificationRouted", "routing-service",
                "ROUTING", "SUCCESS", "SYSTEM",
                Map.of("routeCount", String.valueOf(routes.size()), "checksum", checksum), Instant.now()
        );
        kafkaTemplate.send(NotificationTopics.AUDIT_LIFECYCLE, event.getCorrelationId().toString(), lifecycleEvent);
    }
}
