package com.example.notification.processor.domain.rule;

import com.example.notification.processor.domain.context.NotificationContext;
import org.springframework.stereotype.Component;

@Component
public class BusinessRuleResolver {

    public boolean evaluateApprovalRequirement(NotificationContext context) {
        // Business Rule logic to check if approval is required (e.g. high severity KPI or threshold breach)
        Object severity = context.getKpiData().get("severity");
        if ("CRITICAL".equalsIgnoreCase(String.valueOf(severity))) {
            return true;
        }
        return false;
    }
}
