package com.nihit.craft_connect.config;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class UserDetailConfig {
    private final JwtTokenHelper jwtTokenHelper;

    @Autowired
    private HttpServletRequest request;

    private Map<String, Object> readAccessToken() {

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return Collections.emptyMap();
        }

        String token = authHeader.substring(7);

        return jwtTokenHelper.getAllClaimsAsMap(token);
    }

    public Long getLoggedInUserId() {
        return Long.parseLong(String.valueOf(readAccessToken().get("userId")));
    }
}
