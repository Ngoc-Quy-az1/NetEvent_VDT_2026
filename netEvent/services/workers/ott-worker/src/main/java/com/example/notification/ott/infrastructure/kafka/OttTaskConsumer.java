package com.example.notification.ott.infrastructure.kafka;

import com.example.notification.common.kafka.NotificationTopics;
import com.example.notification.contract.delivery.DeliveryTaskEvent;
import com.example.notification.ott.application.OttDeliveryService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class OttTaskConsumer {

    private final OttDeliveryService ottDeliveryService;

    public OttTaskConsumer(OttDeliveryService ottDeliveryService) {
        this.ottDeliveryService = ottDeliveryService;
    }

    @KafkaListener(topics = NotificationTopics.DELIVERY_OTT, groupId = "ott-worker-group")
    public void consume(DeliveryTaskEvent event) {
        ottDeliveryService.processDeliveryTask(event);
    }
}
