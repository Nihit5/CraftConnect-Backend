package com.nihit.craft_connect.repository;

import com.nihit.craft_connect.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    List<ChatMessage> findByConversation_IdOrderBySentDateAsc(Long conversationId);
    List<ChatMessage> findByConversation_IdAndReceiver_IdAndIsReadFalse(Long conversationId, Long receiverId);

    long countByConversation_IdAndReceiver_IdAndIsReadFalse(Long conversationId, Long receiverId);
}