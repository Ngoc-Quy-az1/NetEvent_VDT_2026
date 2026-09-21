package com.example.notification.common.tracing;

import java.util.UUID;

public class CorrelationIdUtils {
    public static final String CORRELATION_ID_HEADER = "X-Correlation-Id";

    public static String generateCorrelationId() {
        return UUID.randomUUID().toString();
    }
}
