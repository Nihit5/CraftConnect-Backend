package com.nihit.craft_connect.dto.chat;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChatMessageRequestPojo {
    private Long receiverId;
    private String content;
}
