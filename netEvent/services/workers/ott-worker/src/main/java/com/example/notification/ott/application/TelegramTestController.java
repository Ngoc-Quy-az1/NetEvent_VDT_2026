package com.example.notification.ott.application;

import com.example.notification.ott.infrastructure.provider.OttProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/ott/test")
@Tag(name = "Telegram test", description = "Gửi tin nhắn thử nghiệm trực tiếp tới Telegram")
public class TelegramTestController {
    private final OttProvider ottProvider;

    public TelegramTestController(OttProvider ottProvider) {
        this.ottProvider = ottProvider;
    }

    @PostMapping("/message")
    @Operation(
            summary = "Gửi tin nhắn Hello",
            description = "Gửi 'Hello from NetEvent OTT worker!' tới chatId. Nếu không truyền chatId, worker dùng TELEGRAM_CHAT_ID.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Gửi thành công"),
                    @ApiResponse(responseCode = "502", description = "Telegram từ chối hoặc token/chat ID chưa được cấu hình")
            })
    public ResponseEntity<Map<String, Object>> sendMessage(@RequestBody TelegramTestMessageRequest request) {
        if (request == null || request.getMessage() == null || request.getMessage().trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "message is required"));
        }
        boolean sent = ottProvider.sendOtt(request.getChatId(), request.getMessage());
        if (sent) {
            return ResponseEntity.ok(Map.of("success", true, "message", "Telegram message sent"));
        }
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(Map.of(
                "success", false,
                "message", "Telegram message could not be sent. Check bot token and chat ID."));
    }

    @PostMapping(value = "/file", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
            summary = "Send a test file to Telegram",
            description = "Upload a file directly to Telegram. chatId is optional when TELEGRAM_CHAT_ID is configured.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "File sent"),
                    @ApiResponse(responseCode = "400", description = "File is missing or empty"),
                    @ApiResponse(responseCode = "502", description = "Telegram rejected the request or configuration is invalid")
            })
    public ResponseEntity<Map<String, Object>> sendFile(
            @RequestParam(required = false) String chatId,
            @RequestParam(required = false) String caption,
            @RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "file is required"));
        }
        try {
            boolean sent = ottProvider.sendDocument(chatId, file.getOriginalFilename(), file.getBytes(),
                    file.getContentType(), caption);
            if (sent) return ResponseEntity.ok(Map.of("success", true, "message", "Telegram file sent"));
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(Map.of(
                    "success", false,
                    "message", "Telegram file could not be sent. Check bot token and chat ID."));
        } catch (Exception exception) {
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(Map.of(
                    "success", false, "message", "Could not read the uploaded file"));
        }
    }
}
