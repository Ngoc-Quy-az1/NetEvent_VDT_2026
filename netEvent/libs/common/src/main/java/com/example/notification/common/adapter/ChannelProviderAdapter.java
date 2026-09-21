package com.example.notification.common.adapter;

import java.util.Map;

/**
 * Standard Provider Adapter Interface for all Channel Workers (SMS, Email, OTT, MS Teams, etc.)
 */
public interface ChannelProviderAdapter {

    /**
     * Unique code of the channel (e.g. "SMS", "EMAIL", "OTT", "TEAMS")
     */
    String getChannelCode();

    /**
     * Execute actual dispatching of message task to third-party provider gateway
     *
     * @param taskId Unique identifier of the notification task
     * @param contactTarget Target address (phone number, email address, telegram chat_id, teams webhook_url)
     * @param content Rendered message body content
     * @param metadata Optional metadata map
     * @return DeliveryResponse containing status code and response body
     */
    DeliveryResponse send(String taskId, String contactTarget, String content, Map<String, Object> metadata);
}
