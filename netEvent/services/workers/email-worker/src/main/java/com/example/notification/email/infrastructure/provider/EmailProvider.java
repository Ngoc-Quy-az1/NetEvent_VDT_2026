package com.example.notification.email.infrastructure.provider;

public interface EmailProvider {
    boolean sendEmail(String to, String subject, String body);
}
