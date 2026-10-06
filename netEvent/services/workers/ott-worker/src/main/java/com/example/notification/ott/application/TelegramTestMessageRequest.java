package com.example.notification.ott.application;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Telegram test message")
public class TelegramTestMessageRequest {
    @Schema(description = "Telegram chat ID. Leave empty to use TELEGRAM_CHAT_ID.", example = "123456789")
    private String chatId;

    @Schema(description = "Message text to send", example = "KPI alert: cell quality is below threshold.", required = true)
    private String message;

    public String getChatId() { return chatId; }
    public void setChatId(String chatId) { this.chatId = chatId; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
