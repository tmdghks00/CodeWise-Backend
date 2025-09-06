package com.codewise.config;

import com.codewise.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtChannelInterceptor implements ChannelInterceptor {

    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService; // ★ DB 사용자 로드용

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            String token = accessor.getFirstNativeHeader("Authorization");

            if (token == null || !token.startsWith("Bearer ")) {
                throw new IllegalArgumentException("❌ JWT 토큰이 누락되었거나 잘못된 형식입니다.");
            }

            token = token.substring(7); // "Bearer " 제거

            try {
                String email = jwtUtil.getUsername(token); // JWT에서 email 추출

                if (email == null) {
                    throw new IllegalArgumentException("❌ JWT 토큰에 사용자 정보가 없습니다.");
                }

                // ★ DB에서 사용자 조회
                UserDetails userDetails = userDetailsService.loadUserByUsername(email);

                // Authentication 객체 생성 (Principal에 UserDetails 들어감)
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                userDetails, null, userDetails.getAuthorities()
                        );

                accessor.setUser(authentication); // ★ Principal 세팅
                System.out.println(">>> [JwtChannelInterceptor] Principal set with email = " + email);

            } catch (Exception e) {
                throw new IllegalArgumentException("❌ 유효하지 않은 JWT 토큰입니다.", e);
            }
        }
        return message;
    }
}
