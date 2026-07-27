package com.nihit.craft_connect.config;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class JwtHandshakeInterceptor implements HandshakeInterceptor {

    private final JwtTokenHelper jwtTokenHelper;
    private final UserDetailConfig userDetailConfig;
    private final CustomUserDetailService customUserDetailService;

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) {
        if (request instanceof ServletServerHttpRequest servletRequest) {
            String token = servletRequest.getServletRequest().getParameter("token");
            if (token == null) {
                response.setStatusCode(HttpStatus.UNAUTHORIZED);
                return false;
            }
            try {
                String username = jwtTokenHelper.extractUsernameFromToken(token);
                UserDetails userDetails = customUserDetailService.loadUserByUsername(username);
                if (!jwtTokenHelper.validateToken(token, userDetails)) {
                    response.setStatusCode(HttpStatus.UNAUTHORIZED);
                    return false;
                }

                Map<String, Object> claims = jwtTokenHelper.getAllClaimsAsMap(token);
                Long userId = Long.parseLong(String.valueOf(claims.get("userId")));
                attributes.put("userId", userId);
            } catch (Exception e) {
                response.setStatusCode(HttpStatus.UNAUTHORIZED);
                return false;
            }
        }
        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {
        // no-op
    }
}
