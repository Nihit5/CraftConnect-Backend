package com.nihit.craft_connect.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import java.util.List;
import java.util.Map;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class StompPrincipalConfig implements WebSocketMessageBrokerConfigurer {

    private final JwtTokenHelper jwtTokenHelper;
    private final CustomUserDetailService customUserDetailService;

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                StompHeaderAccessor accessor =
                        MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
                if (accessor == null) {
                    accessor = StompHeaderAccessor.wrap(message);
                }

                if (StompCommand.CONNECT.equals(accessor.getCommand())
                        || SimpMessageType.CONNECT.equals(accessor.getMessageType())) {
                    Map<String, Object> sessionAttributes = accessor.getSessionAttributes();
                    Long userId = null;

                    // 1) userId from handshake (legacy ?token= query-param auth)
                    if (sessionAttributes != null) {
                        Object rawUserId = sessionAttributes.get("userId");
                        if (rawUserId != null) {
                            try {
                                userId = Long.parseLong(String.valueOf(rawUserId));
                            } catch (Exception ignored) {
                            }
                        }
                    }

                    // 2) If not present, validate token from STOMP CONNECT header
                    //    (this is the clean, preferred auth path).
                    if (userId == null) {
                        String token = extractStompHeader(accessor, "Authorization", "Bearer ");
                        if (token == null) token = extractStompHeader(accessor, "token", null);
                        if (token != null) {
                            try {
                                String username = jwtTokenHelper.extractUsernameFromToken(token);
                                UserDetails userDetails = customUserDetailService.loadUserByUsername(username);
                                if (jwtTokenHelper.validateToken(token, userDetails)) {
                                    Map<String, Object> claims = jwtTokenHelper.getAllClaimsAsMap(token);
                                    userId = Long.parseLong(String.valueOf(claims.get("userId")));
                                    if (sessionAttributes != null) {
                                        sessionAttributes.put("userId", userId);
                                        sessionAttributes.put("jwtToken", token);
                                    }
                                }
                            } catch (Exception ex) {
                                log.warn("STOMP CONNECT token validation failed: {}", ex.getMessage());
                            }
                        }
                    }

                    if (userId != null) {
                        final Long finalUserId = userId;
                        accessor.setUser(() -> String.valueOf(finalUserId));
                        log.info("STOMP CONNECT authenticated userId={}", userId);
                    } else {
                        log.warn("STOMP CONNECT denied: could not authenticate user from any source");
                    }
                }
                return message;
            }
        });
    }

    private String extractStompHeader(StompHeaderAccessor accessor, String name, String stripPrefix) {
        String raw = accessor.getFirstNativeHeader(name);
        if (raw == null) {
            // Fallback: native headers might be a List (older STOMP libs)
            Object nativeHeaders = accessor.getHeader("nativeHeaders");
            if (nativeHeaders instanceof Map<?, ?> nh) {
                Object rawList = nh.get(name);
                if (rawList instanceof List<?> l && !l.isEmpty()) {
                    raw = String.valueOf(l.get(0));
                }
            }
        }
        if (raw != null && stripPrefix != null && raw.startsWith(stripPrefix)) {
            return raw.substring(stripPrefix.length());
        }
        return raw;
    }
}
