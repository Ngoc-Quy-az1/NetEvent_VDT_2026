package com.example.notification.processor.infrastructure.template;

import org.springframework.stereotype.Component;
import java.util.Map;

@Component
public class TemplateRenderer {

    public String renderTitle(String templateStr, Map<String, Object> data) {
        if (templateStr != null && !templateStr.trim().isEmpty()) {
            return applyDataToTemplate(templateStr, data);
        }
        Object eventCode = data.getOrDefault("eventCode", "NET_EVENT");
        Object profileName = data.getOrDefault("profileName", "COVERAGE_ALERT");
        return String.format("[%s] Cảnh báo chất lượng mạng %s", profileName, eventCode);
    }

    public String renderContent(String templateStr, Map<String, Object> data) {
        if (templateStr != null && !templateStr.trim().isEmpty()) {
            return applyDataToTemplate(templateStr, data);
        }
        StringBuilder sb = new StringBuilder();
        sb.append("BÁO CÁO CẢNH BÁO CHẤT LƯỢNG MẠNG\n");
        sb.append("Mã sự kiện: ").append(data.getOrDefault("eventCode", "N/A")).append("\n");
        sb.append("Loại sự kiện: ").append(data.getOrDefault("eventName", "Cảnh báo KPI Vùng phủ sóng")).append("\n");
        sb.append("Chi tiết KPI:\n");
        data.forEach((k, v) -> {
            if (!"rawPayload".equals(k)) {
                sb.append(" - ").append(k).append(": ").append(v).append("\n");
            }
        });
        return sb.toString();
    }

    private String applyDataToTemplate(String template, Map<String, Object> data) {
        String result = template;
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            String placeholder = "{" + entry.getKey() + "}";
            if (result.contains(placeholder)) {
                result = result.replace(placeholder, String.valueOf(entry.getValue()));
            }
        }
        return result;
    }
}
