package com.nihit.craft_connect.service.chat;

import com.nihit.craft_connect.dto.chat.ChatMessageResponsePojo;
import com.nihit.craft_connect.dto.chat.ConversationResponsePojo;

import java.util.List;

public interface ChatService {
    ChatMessageResponsePojo sendMessage(Long senderId, Long receiverId, String content);
    List<ChatMessageResponsePojo> getConversationHistory(Long conversationId, Long requesterId);
    List<ConversationResponsePojo> getMyConversations(Long userId);
    void markAsRead(Long conversationId, Long readerId);
}