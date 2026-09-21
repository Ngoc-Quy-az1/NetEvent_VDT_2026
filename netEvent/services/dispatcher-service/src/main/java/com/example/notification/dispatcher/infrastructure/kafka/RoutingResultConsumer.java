package com.example.notification.dispatcher.infrastructure.kafka;

import com.example.notification.common.kafka.NotificationTopics;
import com.example.notification.contract.routing.RoutingResultEvent;
import com.example.notification.dispatcher.application.MessageDispatcherService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class RoutingResultConsumer {

    private final MessageDispatcherService messageDispatcherService;

    public RoutingResultConsumer(MessageDispatcherService messageDispatcherService) {
        this.messageDispatcherService = messageDispatcherService;
    }

    @KafkaListener(topics = NotificationTopics.NOTIFICATION_ROUTED, groupId = "dispatcher-service-group")
    public void consume(RoutingResultEvent event) {
        messageDispatcherService.dispatch(event);
    }
}
