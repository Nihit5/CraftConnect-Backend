package com.nihit.craft_connect.service.chat.impl;

import com.nihit.craft_connect.dto.chat.ChatMessageResponsePojo;
import com.nihit.craft_connect.dto.chat.ConversationResponsePojo;
import com.nihit.craft_connect.entity.ChatMessage;
import com.nihit.craft_connect.entity.Conversation;
import com.nihit.craft_connect.entity.User;
import com.nihit.craft_connect.exception.AppException;
import com.nihit.craft_connect.repository.ChatMessageRepository;
import com.nihit.craft_connect.repository.ConversationRepository;
import com.nihit.craft_connect.repository.UserRepository;
import com.nihit.craft_connect.service.chat.ChatService;
import com.nihit.craft_connect.service.file.FileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Timestamp;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    private final ConversationRepository conversationRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final FileService fileService;

    /**
     * Wrap a payload in a standardized STOMP event envelope so the frontend
     * parser always sees { event, data, messages } shape — matches the
     * parseIncomingFrame expectations on the client for Messenger-like UX.
     */
    private Map<String, Object> envelope(String event, Object payload) {
        Map<String, Object> env = new HashMap<>(8);
        env.put("event", event);
        env.put("type", event);
        env.put("data", payload);
        env.put("payload", payload);
        env.put("timestamp", System.currentTimeMillis());
        if (payload != null) {
            env.put("messages", List.of(payload));
        }
        return env;
    }

    /**
     * Resolve the Java compile-time ambiguity on
     *     SimpMessagingTemplate.convertAndSend(destination, payload)
     * when the payload itself is a Map<String,Object>: the generic method
     * <D> convertAndSend(D, Object) matches the same erasure as
     * convertAndSend(Object, Map<String,Object> headers).
     * Casting the destination to the narrow type String on the call-site
     * forces the compiler to pick the (String destination, Object payload)
     * overload exactly as intended.
     */
    private static String asDest(String s) {
        return s;
    }

    private void debugReport(String hypothesisId, String location, String msg, Map<String, Object> data) {
        try {
            URL url = new URL("http://127.0.0.1:7777/event");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setConnectTimeout(300);
            conn.setReadTimeout(300);
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/json");
            String payload = String.format(
                    "{\"sessionId\":\"chat-unread-reset\",\"runId\":\"pre-fix\",\"hypothesisId\":\"%s\",\"location\":\"%s\",\"msg\":\"%s\",\"data\":%s,\"ts\":%d}",
                    escapeJson(hypothesisId),
                    escapeJson(location),
                    escapeJson(msg),
                    toJsonObject(data),
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

    private String toJsonObject(Map<String, Object> data) {
        if (data == null || data.isEmpty()) return "{}";
        StringBuilder sb = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            if (!first) sb.append(',');
            first = false;
            sb.append('"').append(escapeJson(entry.getKey())).append('"').append(':');
            Object value = entry.getValue();
            if (value == null) {
                sb.append("null");
            } else if (value instanceof Number || value instanceof Boolean) {
                sb.append(value);
            } else {
                sb.append('"').append(escapeJson(String.valueOf(value))).append('"');
            }
        }
        sb.append('}');
        return sb.toString();
    }

    private String escapeJson(String value) {
        if (value == null) return "";
        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\r", "\\r")
                .replace("\n", "\\n");
    }

    private void sendToTopic(String destination, Map<String, Object> body) {
        messagingTemplate.convertAndSend((String) destination, (Object) body);
    }

    /**
     * Broadcast a new-message event to EVERY reliable channel we have. Order:
     *  1. /user/{userId}/queue/messages (principal-based private queue)
     *  2. /topic/user.{userId}.queue      (fallback topic, no principal required)
     *  3. /topic/conversation.{convId}    (for all viewers of this conversation)
     * The client deduplicates across these channels, so "deliver too many" is
     * strictly better than missing a delivery (the user's #1 complaint: having
     * to reload the page to see messages).
     */
    private void broadcastNewMessage(ChatMessageResponsePojo msg, Long senderId, Long receiverId, Long conversationId) {
        Map<String, Object> env = envelope("NEW_MESSAGE", msg);

        // ---- Receiver (the other party, most likely to miss it without this) ----
        log.info("WS PUSH [NEW_MESSAGE] receiver={} -> /user/{}/queue/messages, msgId={}",
                receiverId, receiverId, msg.getId());
        messagingTemplate.convertAndSendToUser(String.valueOf(receiverId), "/queue/messages", (Object) env);
        log.info("WS PUSH [NEW_MESSAGE] receiver={} -> /topic/user.{}.queue, msgId={}",
                receiverId, receiverId, msg.getId());
        sendToTopic("/topic/user." + receiverId + ".queue", env);

        // ---- Sender (echo-back to replace their optimistic bubble) ----
        log.info("WS PUSH [NEW_MESSAGE] sender={} -> /user/{}/queue/messages, msgId={}",
                senderId, senderId, msg.getId());
        messagingTemplate.convertAndSendToUser(String.valueOf(senderId), "/queue/messages", (Object) env);
        log.info("WS PUSH [NEW_MESSAGE] sender={} -> /topic/user.{}.queue, msgId={}",
                senderId, senderId, msg.getId());
        sendToTopic("/topic/user." + senderId + ".queue", env);

        // ---- Conversation topic (any client with this chat open) ----
        log.info("WS PUSH [NEW_MESSAGE] conv={} -> /topic/conversation.{}, msgId={}",
                conversationId, conversationId, msg.getId());
        sendToTopic("/topic/conversation." + conversationId, env);

        // ---- Conversation-list / unread badge refresh notifications ----
        Map<String, Object> refreshNote = new HashMap<>(6);
        refreshNote.put("event", "CONVERSATIONS_UPDATED");
        refreshNote.put("conversationId", conversationId);
        refreshNote.put("involves", List.of(senderId, receiverId));
        refreshNote.put("lastMessageAt", msg.getSentDate());

        sendToTopic("/topic/user." + receiverId + ".queue", refreshNote);
        sendToTopic("/topic/user." + senderId + ".queue", refreshNote);
        try {
            messagingTemplate.convertAndSendToUser(String.valueOf(receiverId), "/queue/notifications", (Object) refreshNote);
            messagingTemplate.convertAndSendToUser(String.valueOf(senderId), "/queue/notifications", (Object) refreshNote);
        } catch (Exception ignore) {
            // SimpleBroker might not have a registered Principal session; fallbacks above work.
        }
    }

    private void broadcastReadReceipt(Long readerId, Long otherId, Long conversationId, Long unreadCount) {
        Map<String, Object> receipt = new HashMap<>(8);
        receipt.put("event", "READ_RECEIPT");
        receipt.put("type", "READ_RECEIPT");
        receipt.put("conversationId", conversationId);
        receipt.put("readerId", readerId);
        receipt.put("otherId", otherId);
        receipt.put("unreadCount", unreadCount);
        receipt.put("timestamp", System.currentTimeMillis());
//        receipt.put("data", receipt);

        log.info("WS PUSH [READ_RECEIPT] reader={} other={} conv={}", readerId, otherId, conversationId);
        // Send to the OTHER user (the sender of the now-read messages) via
        // every reliable channel we have.
        sendToTopic("/topic/user." + otherId + ".queue", receipt);
        sendToTopic("/topic/conversation." + conversationId, receipt);
        try {
            messagingTemplate.convertAndSendToUser(String.valueOf(otherId), "/queue/notifications", (Object) receipt);
            messagingTemplate.convertAndSendToUser(String.valueOf(otherId), "/queue/messages", (Object) receipt);
        } catch (Exception ignore) { /* principal routing fallback: topic above */ }

        // Also tell the reader their unread count has been zeroed.
        Map<String, Object> readerNote = new HashMap<>(receipt);
        readerNote.put("event", "MARKED_READ_ACK");
        readerNote.put("type", "MARKED_READ_ACK");
        sendToTopic("/topic/user." + readerId + ".queue", readerNote);
    }

    @Override
    @Transactional
    public ChatMessageResponsePojo sendMessage(Long senderId, Long receiverId, String content) {
        if (content == null || content.isBlank()) {
            throw new AppException("Message cannot be empty.");
        }
        if (senderId.equals(receiverId)) {
            throw new AppException("Cannot send a message to yourself.");
        }

        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new AppException("Sender not found."));
        User receiver = userRepository.findById(receiverId)
                .orElseThrow(() -> new AppException("Recipient not found."));

        Conversation conversation = conversationRepository.findBetweenUsers(senderId, receiverId)
                .orElseGet(() -> {
                    Conversation c = new Conversation();
                    c.setUserOne(sender);
                    c.setUserTwo(receiver);
                    c.setCreatedDate(new Timestamp(System.currentTimeMillis()));
                    return c;
                });

        Timestamp now = new Timestamp(System.currentTimeMillis());

        ChatMessage message = new ChatMessage();
        message.setConversation(conversation);
        message.setSender(sender);
        message.setReceiver(receiver);
        message.setContent(content);
        message.setIsRead(false);
        message.setSentDate(now);

        conversation.setLastMessage(content);
        conversation.setLastMessageAt(now);

        conversationRepository.save(conversation);
        chatMessageRepository.save(message);

        ChatMessageResponsePojo response = mapMessage(message);
        broadcastNewMessage(response, senderId, receiverId, conversation.getId());

        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChatMessageResponsePojo> getConversationHistory(Long conversationId, Long requesterId) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new AppException("Conversation not found."));

        boolean isParticipant = conversation.getUserOne().getId().equals(requesterId)
                || conversation.getUserTwo().getId().equals(requesterId);
        if (!isParticipant) {
            throw new AppException("You are not part of this conversation.");
        }

        return chatMessageRepository.findByConversation_IdOrderBySentDateAsc(conversationId)
                .stream().map(this::mapMessage).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ConversationResponsePojo> getMyConversations(Long userId) {
        List<ConversationResponsePojo> result = conversationRepository.findAllForUser(userId).stream().map(c -> {
            User other = c.getUserOne().getId().equals(userId) ? c.getUserTwo() : c.getUserOne();

            long unread = chatMessageRepository
                    .countByConversation_IdAndReceiver_IdAndIsReadFalse(c.getId(), userId);

            ConversationResponsePojo pojo = new ConversationResponsePojo();
            pojo.setConversationId(c.getId());
            pojo.setOtherUserId(other.getId());
            pojo.setOtherUserName(other.getFirstName() + " " + other.getLastName());
            pojo.setOtherUserImage(fileService.extractFileName(other.getDisplayPicturePath()));
            pojo.setOtherUserRole(other.getRole());
            pojo.setLastMessage(c.getLastMessage());
            pojo.setLastMessageAt(c.getLastMessageAt());
            pojo.setUnreadCount(unread);
            return pojo;
        }).toList();
        // #region debug-point C:get-conversations-unread
        debugReport("C", "ChatServiceImpl.java:getMyConversations", "[DEBUG] conversations unread snapshot",
                Map.of(
                        "userId", userId,
                        "conversationCount", result.size(),
                        "snapshot", result.stream()
                                .map(item -> item.getConversationId() + ":" + item.getUnreadCount())
                                .reduce((a, b) -> a + "|" + b)
                                .orElse("")
                ));
        // #endregion
        return result;
    }

    @Override
    @Transactional
    public void markAsRead(Long conversationId, Long readerId) {
        log.info("Mark as read api hit, {}", readerId);
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new AppException("Conversation not found."));

        boolean isParticipant = conversation.getUserOne().getId().equals(readerId)
                || conversation.getUserTwo().getId().equals(readerId);
        log.info("participant", isParticipant);
        if (!isParticipant) {
            throw new AppException("You are not part of this conversation.");
        }

        List<ChatMessage> unread = chatMessageRepository
                .findByConversation_IdAndReceiver_IdAndIsReadFalse(conversationId, readerId);
        // #region debug-point B:mark-as-read-before-save
        debugReport("B", "ChatServiceImpl.java:markAsRead:beforeSave", "[DEBUG] markAsRead unread loaded",
                Map.of(
                        "conversationId", conversationId,
                        "readerId", readerId,
                        "unreadCountBefore", unread.size()
                ));
        // #endregion
        if (unread.isEmpty()) {
            return; // nothing to update — avoid no-op WS broadcasts
        }
        unread.forEach(m -> m.setIsRead(true));
        chatMessageRepository.saveAll(unread);
        long readerRemainingUnread = chatMessageRepository
                .countByConversation_IdAndReceiver_IdAndIsReadFalse(conversationId, readerId);
        // #region debug-point B:mark-as-read-after-save
        debugReport("B", "ChatServiceImpl.java:markAsRead:afterSave", "[DEBUG] markAsRead saved unread update",
                Map.of(
                        "conversationId", conversationId,
                        "readerId", readerId,
                        "readerRemainingUnread", readerRemainingUnread
                ));
        // #endregion

        // Identify the OTHER user (the original sender of the now-read messages)
        // so we can push a read-receipt event to them: their "Sent ✓" becomes "Read ✓✓".
        Long otherId = conversation.getUserOne().getId().equals(readerId)
                ? conversation.getUserTwo().getId()
                : conversation.getUserOne().getId();

        long remainingUnread = chatMessageRepository
                .countByConversation_IdAndReceiver_IdAndIsReadFalse(conversationId, otherId);

        try {
            broadcastReadReceipt(readerId, otherId, conversationId, remainingUnread);
        } catch (Exception e) {
            log.warn("Failed to broadcast read receipt for conv={}", conversationId, e);
        }
    }

    private ChatMessageResponsePojo mapMessage(ChatMessage m) {
        ChatMessageResponsePojo pojo = new ChatMessageResponsePojo();
        pojo.setId(m.getId());
        pojo.setConversationId(m.getConversation().getId());
        pojo.setSenderId(m.getSender().getId());
        pojo.setSenderName(m.getSender().getFirstName() + " " + m.getSender().getLastName());
        pojo.setReceiverId(m.getReceiver().getId());
        pojo.setContent(m.getContent());
        pojo.setIsRead(m.getIsRead());
        pojo.setSentDate(m.getSentDate());
        return pojo;
    }
}
