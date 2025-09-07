package com.codewise.config;

import com.codewise.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JwtChannelInterceptor implements ChannelInterceptor {

    private static final Logger log = LoggerFactory.getLogger(JwtChannelInterceptor.class);
    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            log.info("WebSocket CONNECT 요청 감지. 헤더 확인 중...");
            String token = accessor.getFirstNativeHeader("Authorization");

            if (token == null || !token.startsWith("Bearer ")) {
                log.error("❌ JWT 토큰이 누락되었거나 잘못된 형식입니다.");
                throw new IllegalArgumentException("❌ JWT 토큰이 누락되었거나 잘못된 형식입니다.");
            }

            token = token.substring(7);
            log.info("추출된 JWT 토큰: {}", token);

            try {
                // JWT에서 email 꺼내기
                String email = jwtUtil.getEmail(token);
                log.info("[JwtChannelInterceptor] JWT에서 추출된 email: {}", email);

                // DB에서 사용자 정보 확인
                UserDetails userDetails = userDetailsService.loadUserByUsername(email);
                log.debug("DB에서 사용자 정보 로드 성공: {}", userDetails.getUsername());

                // Authentication 객체 세팅 (Principal = email)
                UsernamePasswordAuthenticationToken auth =
                        new UsernamePasswordAuthenticationToken(
                                email, null, userDetails.getAuthorities());

                accessor.setUser(auth);
                log.info(">>> [JwtChannelInterceptor] Principal 세팅 완료: {}", email);

            } catch (Exception e) {
                log.error("❌ JWT 검증 실패 또는 사용자 정보 로드 실패.", e);
                throw new IllegalArgumentException("❌ 유효하지 않은 JWT 토큰입니다.", e);
            }
        }
        return message;
    }
}
