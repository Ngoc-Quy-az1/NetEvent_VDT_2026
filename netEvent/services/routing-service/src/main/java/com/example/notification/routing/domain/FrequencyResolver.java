package com.example.notification.routing.domain;

import org.springframework.stereotype.Component;

@Component
public class FrequencyResolver {

    public boolean checkFrequencyLimit(String recipientId, String channel) {
        // Check rate limiting / mute window rules
        return true; // Allowed
    }
}
