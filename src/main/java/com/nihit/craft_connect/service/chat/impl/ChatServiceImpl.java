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
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    private final ConversationRepository conversationRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final FileService fileService;

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

        // push to the receiver's private queue in real time — only fires if they're currently connected
        messagingTemplate.convertAndSendToUser(
                String.valueOf(receiverId), "/queue/messages", response
        );

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
        return conversationRepository.findAllForUser(userId).stream().map(c -> {
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
    }

    @Override
    @Transactional
    public void markAsRead(Long conversationId, Long readerId) {
        List<ChatMessage> unread = chatMessageRepository
                .findByConversation_IdAndReceiver_IdAndIsReadFalse(conversationId, readerId);
        unread.forEach(m -> m.setIsRead(true));
        chatMessageRepository.saveAll(unread);
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