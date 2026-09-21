package com.example.notification.dispatcher.application;

import com.example.notification.common.kafka.NotificationTopics;
import com.example.notification.contract.audit.LifecycleEvent;
import com.example.notification.contract.delivery.DeliveryTaskEvent;
import com.example.notification.contract.routing.RoutingResultEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class MessageDispatcherService {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final Set<String> dispatchedDeduplicationSet = Collections.newSetFromMap(new ConcurrentHashMap<>());

    public MessageDispatcherService(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void dispatch(RoutingResultEvent event) {
        log.info("Dispatcher received RoutingResultEvent: notificationId={}, runId={}",
                event.getNotificationId(), event.getRunId());

        List<RoutingResultEvent.RouteItem> routes = event.getRoutes();
        if (routes == null || routes.isEmpty()) {
            // Fallback route generation if legacy format
            routes = new ArrayList<>();
            List<String> channels = event.getChannels() != null ? event.getChannels() : List.of("EMAIL");
            List<String> recipients = event.getRecipientIds() != null ? event.getRecipientIds() : List.of("REC_NOC_01");
            for (String ch : channels) {
                for (String r : recipients) {
                    routes.add(new RoutingResultEvent.RouteItem(ch, r, r + "@domain.com", event.getContent(), event.getChecksum()));
                }
            }
        }

        int dispatchedCount = 0;
        for (RoutingResultEvent.RouteItem route : routes) {
            String dedupKey = String.format("%s_%s_%s",
                    event.getNotificationId(), route.getChannelCode(), route.getRecipientId());

            if (!dispatchedDeduplicationSet.add(dedupKey)) {
                log.warn("Skipping duplicate delivery task for dedupKey={}", dedupKey);
                continue;
            }

            UUID taskId = UUID.randomUUID();
            String topic = resolveTopicForChannel(route.getChannelCode());

            DeliveryTaskEvent taskEvent = new DeliveryTaskEvent(
                    taskId,
                    event.getNotificationId(),
                    event.getCorrelationId(),
                    event.getRunId(),
                    route.getChannelCode(),
                    route.getRecipientId(),
                    route.getRecipientContact(),
                    event.getTitle() != null ? event.getTitle() : "Cảnh báo chất lượng mạng",
                    route.getContent() != null ? route.getContent() : event.getContent(),
                    route.getChecksum() != null ? route.getChecksum() : event.getChecksum(),
                    1, // initial attempt
                    Collections.emptyMap(),
                    Instant.now()
            );

            log.info("Publishing DeliveryTaskEvent to topic {}: taskId={}, recipient={}",
                    topic, taskId, route.getRecipientContact());
            kafkaTemplate.send(topic, taskId.toString(), taskEvent);
            dispatchedCount++;
        }

        LifecycleEvent lifecycleEvent = new LifecycleEvent(
                event.getCorrelationId(), event.getRunId(), "TasksDispatched", "dispatcher-service",
                "DISPATCHING", "SUCCESS", "SYSTEM",
                Map.of("dispatchedTasks", String.valueOf(dispatchedCount)), Instant.now()
        );
        kafkaTemplate.send(NotificationTopics.AUDIT_LIFECYCLE, event.getCorrelationId().toString(), lifecycleEvent);
    }

    private String resolveTopicForChannel(String channel) {
        if (channel == null) return NotificationTopics.DELIVERY_EMAIL;
        switch (channel.toUpperCase()) {
            case "SMS":
                return NotificationTopics.DELIVERY_SMS;
            case "EMAIL":
                return NotificationTopics.DELIVERY_EMAIL;
            case "OTT":
            case "TELEGRAM":
                return NotificationTopics.DELIVERY_OTT;
            default:
                return NotificationTopics.DELIVERY_EMAIL;
        }
    }
}
