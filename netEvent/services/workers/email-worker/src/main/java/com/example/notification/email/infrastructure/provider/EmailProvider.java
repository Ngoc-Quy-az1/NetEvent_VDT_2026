package com.example.notification.email.infrastructure.provider;

import com.example.notification.contract.delivery.AttachmentRef;
import java.util.List;

public interface EmailProvider {
    boolean sendEmail(String to, String subject, String body);
    boolean sendEmail(String to, String subject, String body, List<AttachmentRef> attachments);
}
