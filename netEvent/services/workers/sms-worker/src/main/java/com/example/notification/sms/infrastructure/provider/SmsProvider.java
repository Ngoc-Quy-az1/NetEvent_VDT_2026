package com.example.notification.sms.infrastructure.provider;

public interface SmsProvider {
    boolean sendSms(String phone, String message);
}
