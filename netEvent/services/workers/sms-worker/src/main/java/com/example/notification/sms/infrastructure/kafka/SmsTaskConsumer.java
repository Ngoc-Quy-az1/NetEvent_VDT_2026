package com.example.notification.sms.infrastructure.kafka;

import com.example.notification.common.kafka.NotificationTopics;
import com.example.notification.contract.delivery.DeliveryTaskEvent;
import com.example.notification.sms.application.SmsDeliveryService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class SmsTaskConsumer {

    private final SmsDeliveryService smsDeliveryService;

    public SmsTaskConsumer(SmsDeliveryService smsDeliveryService) {
        this.smsDeliveryService = smsDeliveryService;
    }

    @KafkaListener(topics = NotificationTopics.DELIVERY_SMS, groupId = "sms-worker-group")
    public void consume(DeliveryTaskEvent event) {
        smsDeliveryService.processDeliveryTask(event);
    }
}
