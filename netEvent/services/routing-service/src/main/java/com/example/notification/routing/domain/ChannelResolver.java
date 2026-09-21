package com.example.notification.routing.domain;

import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class ChannelResolver {

    public List<String> resolveChannels(String eventType) {
        // Resolve appropriate notification channels based on event type / priority
        return List.of("SMS", "EMAIL", "OTT");
    }
}
