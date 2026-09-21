package com.example.notification.ott.infrastructure.provider;

import org.springframework.stereotype.Component;

@Component
public class TelegramOttProvider implements OttProvider {

    @Override
    public boolean sendOtt(String target, String message) {
        // Telegram Bot API integration
        return true;
    }
}
