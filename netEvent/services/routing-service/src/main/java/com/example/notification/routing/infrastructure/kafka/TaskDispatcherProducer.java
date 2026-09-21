package com.example.notification.routing.infrastructure.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@Slf4j
@RequiredArgsConstructor
public class TaskDispatcherProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${app.kafka.topics.sms-task:notification.sms.task}")
    private String smsTopic;

    @Value("${app.kafka.topics.email-task:notification.email.task}")
    private String emailTopic;

    @Value("${app.kafka.topics.ott-task:notification.ott.task}")
    private String ottTopic;

    public void dispatchSmsTask(Map<String, Object> taskPayload) {
        log.info("Dispatching SMS Task to Kafka topic: {}", smsTopic);
        kafkaTemplate.send(smsTopic, taskPayload);
    }

    public void dispatchEmailTask(Map<String, Object> taskPayload) {
        log.info("Dispatching Email Task to Kafka topic: {}", emailTopic);
        kafkaTemplate.send(emailTopic, taskPayload);
    }

    public void dispatchOttTask(Map<String, Object> taskPayload) {
        log.info("Dispatching OTT/Telegram Task to Kafka topic: {}", ottTopic);
        kafkaTemplate.send(ottTopic, taskPayload);
    }
}
