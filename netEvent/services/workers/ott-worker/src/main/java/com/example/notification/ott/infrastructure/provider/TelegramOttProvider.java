package com.example.notification.ott.infrastructure.provider;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.web.client.RestTemplate;

import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

@Component
@Slf4j
public class TelegramOttProvider implements OttProvider {
    private static final int TELEGRAM_MESSAGE_LIMIT = 4096;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${telegram.bot-token:}")
    private String botToken;

    @Value("${telegram.default-chat-id:}")
    private String defaultChatId;

    @Override
    public boolean sendOtt(String target, String message) {
        if (!StringUtils.hasText(botToken)) {
            log.error("Telegram delivery skipped: TELEGRAM_BOT_TOKEN is not configured");
            return false;
        }

        String chatId = resolveChatId(target);
        if (!StringUtils.hasText(chatId)) {
            log.error("Telegram delivery skipped: no recipient chat ID was supplied");
            return false;
        }

        String content = StringUtils.hasText(message) ? message : "(Không có nội dung thông báo)";
        for (int start = 0; start < content.length(); start += TELEGRAM_MESSAGE_LIMIT) {
            int end = Math.min(content.length(), start + TELEGRAM_MESSAGE_LIMIT);
            if (!sendChunk(chatId, content.substring(start, end))) return false;
        }
        return true;
    }

    @Override
    public boolean sendDocument(String target, String fileName, byte[] content, String contentType, String caption) {
        if (!StringUtils.hasText(botToken)) {
            log.error("Telegram document delivery skipped: TELEGRAM_BOT_TOKEN is not configured");
            return false;
        }
        String chatId = resolveChatId(target);
        if (!StringUtils.hasText(chatId) || content == null || content.length == 0) {
            log.error("Telegram document delivery skipped: chat ID or file content is missing");
            return false;
        }

        try {
            ByteArrayResource resource = new ByteArrayResource(content) {
                @Override
                public String getFilename() {
                    return StringUtils.hasText(fileName) ? fileName : "attachment";
                }
            };
            HttpHeaders fileHeaders = new HttpHeaders();
            fileHeaders.setContentType(StringUtils.hasText(contentType)
                    ? MediaType.parseMediaType(contentType) : MediaType.APPLICATION_OCTET_STREAM);

            MultiValueMap<String, Object> form = new LinkedMultiValueMap<>();
            form.add("chat_id", chatId);
            form.add("document", new HttpEntity<>(resource, fileHeaders));
            if (StringUtils.hasText(caption)) form.add("caption", caption);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            String response = restTemplate.postForObject(
                    "https://api.telegram.org/bot" + botToken + "/sendDocument",
                    new HttpEntity<>(form, headers), String.class);
            JsonNode result = objectMapper.readTree(response);
            if (result.path("ok").asBoolean(false)) return true;

            log.error("Telegram rejected document for chat {}: {}", chatId, result.path("description").asText("unknown error"));
        } catch (Exception exception) {
            log.error("Telegram document send failed for chat {}: {}", chatId, exception.getMessage());
        }
        return false;
    }

    private String resolveChatId(String target) {
        return StringUtils.hasText(target) ? target.trim() : defaultChatId;
    }

    private boolean sendChunk(String chatId, String text) {
        try {
            Map<String, Object> request = new LinkedHashMap<>();
            request.put("chat_id", chatId);
            request.put("text", text);
            request.put("disable_web_page_preview", true);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            String response = restTemplate.postForObject(
                    "https://api.telegram.org/bot" + botToken + "/sendMessage",
                    new HttpEntity<>(request, headers), String.class);
            JsonNode result = objectMapper.readTree(response);
            if (result.path("ok").asBoolean(false)) return true;

            log.error("Telegram rejected message for chat {}: {}", chatId, result.path("description").asText("unknown error"));
        } catch (Exception exception) {
            // Do not include the URL in logs because it contains the bot token.
            log.error("Telegram send failed for chat {}: {}", chatId, exception.getMessage());
        }
        return false;
    }
}
