package com.example.notification.email.infrastructure.kafka;

import com.example.notification.common.kafka.NotificationTopics;
import com.example.notification.contract.delivery.DeliveryTaskEvent;
import com.example.notification.email.application.EmailDeliveryService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class EmailTaskConsumer {

    private final EmailDeliveryService emailDeliveryService;

    public EmailTaskConsumer(EmailDeliveryService emailDeliveryService) {
        this.emailDeliveryService = emailDeliveryService;
    }

    @KafkaListener(topics = NotificationTopics.DELIVERY_EMAIL, groupId = "email-worker-group")
    public void consume(DeliveryTaskEvent event) {
        emailDeliveryService.processDeliveryTask(event);
    }
}
