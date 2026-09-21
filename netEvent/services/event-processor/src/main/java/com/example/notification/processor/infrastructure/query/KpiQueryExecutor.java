package com.example.notification.processor.infrastructure.query;

import org.springframework.stereotype.Component;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Component
public class KpiQueryExecutor {

    public Map<String, Object> queryKpiContext(UUID profileId, Map<String, Object> rawPayload) {
        Map<String, Object> context = new HashMap<>(rawPayload);
        context.put("queriedAt", System.currentTimeMillis());
        context.put("status", "RESOLVED");
        return context;
    }
}
