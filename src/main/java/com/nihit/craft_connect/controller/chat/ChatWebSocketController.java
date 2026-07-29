package com.nihit.craft_connect.controller.chat;

import com.nihit.craft_connect.dto.chat.ChatMessageRequestPojo;
import com.nihit.craft_connect.exception.AppException;
import com.nihit.craft_connect.service.chat.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Controller
@RequiredArgsConstructor
public class ChatWebSocketController {

    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;

    private Map<String, Object> envelope(String event, Object payload) {
        Map<String, Object> env = new HashMap<>(8);
        env.put("event", event);
        env.put("type", event);
        env.put("data", payload);
        env.put("payload", payload);
        env.put("timestamp", System.currentTimeMillis());
        return env;
    }

    /**
     * Resolve the Java compile-time ambiguity on convertAndSend / convertAndSendToUser
     * when the payload itself is a Map<String,Object>:
     *   <D> convertAndSend(D dest,       Object payload)
     *       convertAndSend(Object payload, Map<String,Object> headers)
     * would otherwise match the same call-site shape. Explicit casts force the
     * (destination, payload) overloads as intended.
     */
    private void sendToTopic(String destination, Map<String, Object> body) {
        messagingTemplate.convertAndSend((String) destination, (Object) body);
    }

    private void sendToUser(String userName, String queue, Map<String, Object> body) {
        messagingTemplate.convertAndSendToUser(userName, queue, (Object) body);
    }

    /**
     * Send an error/event frame back to a STOMP client identified by senderId (as
     * Principal name) AND via the session-scoped /topic/session.{sessionId}.errors
     * fallback, because convertAndSendToUser requires Principal<->session mapping
     * to be fully registered which may not be true on CONNECT-race errors.
     */
    private void pushToClient(Long senderId, String sessionId, String queue, Map<String, Object> body) {
        if (senderId != null) {
            try {
                sendToUser(String.valueOf(senderId), queue, body);
            } catch (Exception ex) {
                log.warn("sendToUser failed userId={} queue={}: {}", senderId, queue, ex.getMessage());
            }
            // Fallback topic: /topic/user.{senderId}.queue — frontend subscribes this
            // on connect so Principal routing failures don't lose error frames.
            try {
                sendToTopic("/topic/user." + senderId + ".queue", body);
            } catch (Exception ignore) { /* ok */ }
        }
        // Session-scoped topic fallback: even when no Principal exists, the client
        // can subscribe /topic/session.{sessionId}.errors on CONNECTED frame.
        if (sessionId != null) {
            try {
                sendToTopic("/topic/session." + sessionId + ".errors", body);
            } catch (Exception ignore) { /* ok */ }
        }
    }

    // client sends to: /app/chat.send
    @MessageMapping("/chat.send")
    public void sendMessage(ChatMessageRequestPojo request, SimpMessageHeaderAccessor headerAccessor) {
        Long senderId = resolveSenderId(headerAccessor);
        String sessionId = headerAccessor.getSessionId();

        if (senderId == null) {
            log.warn("/app/chat.send dropped — unauthenticated STOMP session. " +
                            "receiverId={}, contentPreview={}",
                    request.getReceiverId(),
                    request.getContent() == null ? null :
                            request.getContent().substring(0, Math.min(40, request.getContent().length())));
            Map<String, Object> err = envelope("SEND_FAILED",
                    Map.of("event", "SEND_FAILED",
                            "error", "Not authenticated",
                            "receiverId", request.getReceiverId(),
                            "content", request.getContent()));
            // If we can figure out the sender's session, use fallback topics.
            if (sessionId != null) {
                try {
                    sendToTopic("/topic/session." + sessionId + ".errors", err);
                } catch (Exception ignore) { /* ok */ }
            }
            return;
        }
        if (request.getReceiverId() == null) {
            Map<String, Object> err = envelope("SEND_FAILED",
                    Map.of("event", "SEND_FAILED",
                            "error", "receiverId is required",
                            "receiverId", null,
                            "content", request.getContent()));
            pushToClient(senderId, sessionId, "/queue/errors", err);
            throw new AppException("receiverId is required");
        }
        if (request.getContent() == null || request.getContent().isBlank()) {
            Map<String, Object> err = envelope("SEND_FAILED",
                    Map.of("event", "SEND_FAILED",
                            "error", "Message content cannot be empty",
                            "receiverId", request.getReceiverId()));
            pushToClient(senderId, sessionId, "/queue/errors", err);
            throw new AppException("Message content cannot be empty");
        }
        try {
            chatService.sendMessage(senderId, request.getReceiverId(), request.getContent());
        } catch (Exception ex) {
            log.error("/app/chat.send failed sender={} receiver={}",
                    senderId, request.getReceiverId(), ex);
            Map<String, Object> err = envelope("SEND_FAILED",
                    Map.of("event", "SEND_FAILED",
                            "error", ex.getMessage() != null ? ex.getMessage() : "Send failed",
                            "receiverId", request.getReceiverId(),
                            "content", request.getContent()));
            pushToClient(senderId, sessionId, "/queue/errors", err);
            throw ex;
        }
    }

    private Long resolveSenderId(SimpMessageHeaderAccessor headerAccessor) {
        // 1) Principal set by StompPrincipalConfig.CONNECT interceptor
        Principal user = headerAccessor.getUser();
        if (user != null && user.getName() != null) {
            try {
                return Long.parseLong(user.getName());
            } catch (NumberFormatException ignored) {
            }
        }
        // 2) Fallback: session attributes from handshake
        Map<String, Object> sessionAttrs = headerAccessor.getSessionAttributes();
        if (sessionAttrs != null) {
            Object rawUserId = sessionAttrs.get("userId");
            if (rawUserId != null) {
                try {
                    return Long.parseLong(String.valueOf(rawUserId));
                } catch (NumberFormatException ignored) {
                }
            }
        }
        return null;
    }
}
