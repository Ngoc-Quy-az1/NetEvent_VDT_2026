package com.example.notification.dispatcher.domain;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
public class DeliveryTaskFactory {

    public List<DeliveryTask> createDeliveryTasks(UUID notificationId, UUID correlationId, String title, String body, List<String> channels, List<String> recipientTargets) {
        List<DeliveryTask> tasks = new ArrayList<>();
        for (String channel : channels) {
            for (String target : recipientTargets) {
                DeliveryTask task = new DeliveryTask(
                        UUID.randomUUID(),
                        notificationId,
                        correlationId,
                        channel.toUpperCase(),
                        target,
                        title,
                        body
                );
                tasks.add(task);
            }
        }
        return tasks;
    }
}
