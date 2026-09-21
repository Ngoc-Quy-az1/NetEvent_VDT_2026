package com.example.notification.routing.domain;

import org.springframework.stereotype.Component;
import java.util.List;
import java.util.UUID;

@Component
public class RecipientResolver {

    public List<String> resolveRecipients(UUID profileId) {
        // Resolve list of recipient targets (emails, phones, device tokens) for given profile
        return List.of("user1@example.com", "+84901234567");
    }
}
