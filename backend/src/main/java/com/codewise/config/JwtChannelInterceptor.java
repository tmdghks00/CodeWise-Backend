package com.codewise.config;

import com.codewise.util.JwtUtil;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class JwtChannelInterceptor implements ChannelInterceptor {

    private final JwtUtil jwtUtil;

    public JwtChannelInterceptor(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        // STOMP 헤더 접근
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

        // CONNECT 시 JWT 검증
        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            String token = accessor.getFirstNativeHeader("Authorization");

            if (token == null || !token.startsWith("Bearer ")) {
                throw new IllegalArgumentException("❌ JWT 토큰이 누락되었거나 잘못된 형식입니다.");
            }

            token = token.substring(7); // "Bearer " 제거

            try {
                // 여기서는 getUsername 사용
                String username = jwtUtil.getUsername(token);

                if (username == null) {
                    throw new IllegalArgumentException("❌ JWT 토큰에 사용자 정보가 없습니다.");
                }

                accessor.setUser(new UsernamePasswordAuthenticationToken(username, null, List.of()));

            } catch (Exception e) {
                throw new IllegalArgumentException("❌ 유효하지 않은 JWT 토큰입니다.", e);
            }
        }

        return message;
    }
}
