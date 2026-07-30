package com.nihit.craft_connect.controller.chat;

import com.nihit.craft_connect.config.CustomMessageSource;
import com.nihit.craft_connect.config.UserDetailConfig;
import com.nihit.craft_connect.constants.SuccessConstants;
import com.nihit.craft_connect.controller.BaseController;
import com.nihit.craft_connect.dto.GlobalApiResponse;
import com.nihit.craft_connect.dto.chat.ChatMessageRequestPojo;
import com.nihit.craft_connect.service.chat.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/v1/chat")
@RequiredArgsConstructor
public class ChatController extends BaseController {

    private final ChatService chatService;
    private final UserDetailConfig userDetailConfig;
    private final CustomMessageSource customMessageSource;

    private void debugReport(Long userId, Long conversationId) {
        try {
            URL url = new URL("http://127.0.0.1:7777/event");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setConnectTimeout(300);
            conn.setReadTimeout(300);
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/json");
            String payload = String.format(
                    "{\"sessionId\":\"chat-unread-reset\",\"runId\":\"pre-fix\",\"hypothesisId\":\"A\",\"location\":\"ChatController.java:markRead\",\"msg\":\"[DEBUG] markRead endpoint invoked\",\"data\":{\"userId\":%d,\"conversationId\":%d},\"ts\":%d}",
                    userId,
                    conversationId,
                    System.currentTimeMillis()
            );
            try (OutputStream os = conn.getOutputStream()) {
                os.write(payload.getBytes(StandardCharsets.UTF_8));
            }
            conn.getResponseCode();
            conn.disconnect();
        } catch (Exception ignore) {
            // instrumentation must never affect business flow
        }
    }

    private void debugAttachmentReport(String hypothesisId, String location, String message, String dataJson) {
        try {
            URL url = new URL("http://127.0.0.1:7777/event");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setConnectTimeout(300);
            conn.setReadTimeout(300);
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/json");
            String payload = String.format(
                    "{\"sessionId\":\"chat-attachment-send\",\"runId\":\"pre-fix\",\"hypothesisId\":\"%s\",\"location\":\"%s\",\"msg\":\"%s\",\"data\":%s,\"ts\":%d}",
                    hypothesisId,
                    location,
                    message,
                    dataJson,
                    System.currentTimeMillis()
            );
            try (OutputStream os = conn.getOutputStream()) {
                os.write(payload.getBytes(StandardCharsets.UTF_8));
            }
            conn.getResponseCode();
            conn.disconnect();
        } catch (Exception ignore) {
            // instrumentation must never affect business flow
        }
    }

    @GetMapping("/conversations")
    public ResponseEntity<GlobalApiResponse> getMyConversations() {
        Long userId = userDetailConfig.getLoggedInUserId();
        return ResponseEntity.ok(successResponse(
                customMessageSource.get(SuccessConstants.SUCCESS_RETRIEVE, "Conversations"),
                chatService.getMyConversations(userId)));
    }

    @GetMapping("/conversations/{conversationId}/messages")
    public ResponseEntity<GlobalApiResponse> getHistory(@PathVariable Long conversationId) {
        Long userId = userDetailConfig.getLoggedInUserId();
        return ResponseEntity.ok(successResponse(
                customMessageSource.get(SuccessConstants.SUCCESS_RETRIEVE, "Messages"),
                chatService.getConversationHistory(conversationId, userId)));
    }

    @PutMapping("/conversations/{conversationId}/read")
    public ResponseEntity<GlobalApiResponse> markRead(@PathVariable Long conversationId) {
        Long userId = userDetailConfig.getLoggedInUserId();
        // #region debug-point A:mark-read-endpoint
        debugReport(userId, conversationId);
        // #endregion
        chatService.markAsRead(conversationId, userId);
        return ResponseEntity.ok(successResponse(
                customMessageSource.get(SuccessConstants.SUCCESS_UPDATE, "Messages"), null));
    }

    // fallback for sending over plain REST (e.g. push-notification-only clients, or initial testing)
    @PostMapping("/send")
    public ResponseEntity<GlobalApiResponse> sendMessage(@RequestBody ChatMessageRequestPojo request) {
        Long userId = userDetailConfig.getLoggedInUserId();
        return ResponseEntity.ok(successResponse(
                "Message sent successfully!",
                chatService.sendMessage(userId, request.getReceiverId(), request.getContent())));
    }

    @PostMapping(value = "/send-attachment", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<GlobalApiResponse> sendAttachment(
            @RequestParam Long receiverId,
            @RequestParam(required = false) String content,
            @RequestParam("attachment") MultipartFile attachment
    ) {
        Long userId = userDetailConfig.getLoggedInUserId();
        // #region debug-point B:send-attachment-controller-entry
        debugAttachmentReport(
                "B",
                "ChatController.java:sendAttachment:entry",
                "[DEBUG] send attachment endpoint invoked",
                String.format(
                        "{\"userId\":%d,\"receiverId\":%d,\"hasContent\":%s,\"attachmentPresent\":%s,\"attachmentEmpty\":%s,\"attachmentName\":%s,\"attachmentSize\":%s}",
                        userId,
                        receiverId,
                        content != null && !content.isBlank(),
                        attachment != null,
                        attachment == null ? "null" : attachment.isEmpty(),
                        attachment != null && attachment.getOriginalFilename() != null ? "\"" + attachment.getOriginalFilename().replace("\"", "\\\"") + "\"" : "null",
                        attachment != null ? attachment.getSize() : null
                )
        );
        // #endregion
        return ResponseEntity.ok(successResponse(
                "Attachment sent successfully!",
                chatService.sendMessage(userId, receiverId, content, attachment)));
    }
}
