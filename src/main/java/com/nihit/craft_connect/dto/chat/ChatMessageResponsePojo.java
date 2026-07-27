package com.nihit.craft_connect.dto.chat;

import lombok.Getter;
import lombok.Setter;
import java.sql.Timestamp;

@Getter
@Setter
public class ChatMessageResponsePojo {
    private Long id;
    private Long conversationId;
    private Long senderId;
    private String senderName;
    private Long receiverId;
    private String content;
    private Boolean isRead;
    private Timestamp sentDate;
}
