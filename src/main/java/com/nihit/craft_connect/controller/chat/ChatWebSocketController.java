package com.nihit.craft_connect.controller.chat;

import com.nihit.craft_connect.dto.chat.ChatMessageRequestPojo;
import com.nihit.craft_connect.service.chat.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;

import java.util.Objects;

@Controller
@RequiredArgsConstructor
public class ChatWebSocketController {

    private final ChatService chatService;

    // client sends to: /app/chat.send
    @MessageMapping("/chat.send")
    public void sendMessage(ChatMessageRequestPojo request, SimpMessageHeaderAccessor headerAccessor) {
        Long senderId = Long.valueOf(Objects.requireNonNull(headerAccessor.getUser()).getName()); // set during handshake/CONNECT
        chatService.sendMessage(senderId, request.getReceiverId(), request.getContent());
        // no need to also send back to sender here — the REST-triggered send below handles
        // the "message appears for the sender immediately" case if you also expose a REST fallback
    }
}
