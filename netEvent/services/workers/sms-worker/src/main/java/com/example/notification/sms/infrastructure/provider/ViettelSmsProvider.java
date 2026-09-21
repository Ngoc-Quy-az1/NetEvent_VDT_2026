package com.example.notification.sms.infrastructure.provider;

import org.springframework.stereotype.Component;

@Component
public class ViettelSmsProvider implements SmsProvider {

    @Override
    public boolean sendSms(String phone, String message) {
        // SMS Gateway Integration
        return true;
    }
}
