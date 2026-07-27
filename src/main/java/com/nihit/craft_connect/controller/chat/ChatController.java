package com.nihit.craft_connect.controller.chat;

import com.nihit.craft_connect.config.CustomMessageSource;
import com.nihit.craft_connect.config.UserDetailConfig;
import com.nihit.craft_connect.constants.SuccessConstants;
import com.nihit.craft_connect.controller.BaseController;
import com.nihit.craft_connect.dto.GlobalApiResponse;
import com.nihit.craft_connect.dto.chat.ChatMessageRequestPojo;
import com.nihit.craft_connect.service.chat.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/v1/chat")
@RequiredArgsConstructor
public class ChatController extends BaseController {

    private final ChatService chatService;
    private final UserDetailConfig userDetailConfig;
    private final CustomMessageSource customMessageSource;

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
}