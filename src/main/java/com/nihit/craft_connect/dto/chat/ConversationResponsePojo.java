package com.nihit.craft_connect.dto.chat;

import lombok.Getter;
import lombok.Setter;
import java.sql.Timestamp;

@Getter
@Setter
public class ConversationResponsePojo {
    private Long conversationId;
    private Long otherUserId;
    private String otherUserName;
    private String otherUserImage;
    private String otherUserRole;
    private String lastMessage;
    private Timestamp lastMessageAt;
    private Long unreadCount;
}