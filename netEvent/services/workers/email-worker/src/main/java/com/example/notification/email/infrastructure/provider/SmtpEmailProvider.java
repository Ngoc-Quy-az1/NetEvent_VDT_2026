package com.example.notification.email.infrastructure.provider;

import org.springframework.stereotype.Component;

@Component
public class SmtpEmailProvider implements EmailProvider {

    @Override
    public boolean sendEmail(String to, String subject, String body) {
        // SMTP email sending logic
        return true;
    }
}
