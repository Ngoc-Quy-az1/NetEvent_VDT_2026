package com.example.notification.sms.infrastructure.persistence;

import org.springframework.stereotype.Repository;
import java.time.Instant;
import java.util.UUID;

@Repository
public class DeliveryLogRepository {

    public void logAttempt(UUID taskId, String channel, String provider, int responseCode, String status, Instant startedAt, Instant finishedAt) {
        // Log SMS delivery attempt
    }
}
