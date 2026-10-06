package com.example.notification.common.kafka;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class NotificationTopics {
    public static final String PROFILE_TRIGGERED = "profile.triggered.v1";
    public static final String PROFILE_PROCESSING_RESULT = "profile.processing-result.v1";
    public static final String APPROVAL_REQUESTED = "notification.approval-requested.v1";
    public static final String NOTIFICATION_APPROVED = "notification.approved.v1";
    public static final String NOTIFICATION_REJECTED = "notification.rejected.v1";
    public static final String NOTIFICATION_TIMEOUT = "notification.timeout.v1";
    public static final String NOTIFICATION_ROUTED = "notification.routed.v1";
    
    public static final String DELIVERY_EMAIL = "delivery.email.v1";
    public static final String DELIVERY_SMS = "delivery.sms.v1";
    public static final String DELIVERY_OTT = "delivery.ott.v1";
    
    public static final String AUDIT_LIFECYCLE = "audit.lifecycle-event.v1";

    public static String calculateChecksum(String content) {
        if (content == null) return "";
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(content.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not found", e);
        }
    }
}
