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

@Component
@RequiredArgsConstructor
public class JwtChannelInterceptor implements ChannelInterceptor {

    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            String token = accessor.getFirstNativeHeader("Authorization");

            if (token == null || !token.startsWith("Bearer ")) {
                throw new IllegalArgumentException("❌ JWT 토큰이 누락되었거나 잘못된 형식입니다.");
            }

            token = token.substring(7);

            try {
                // JWT에서 이메일 추출
                String email = jwtUtil.getUsername(token);

                // DB에서 사용자 정보 로드
                UserDetails userDetails = userDetailsService.loadUserByUsername(email);

                // Authentication 객체 생성 후 Principal 세팅
                UsernamePasswordAuthenticationToken auth =
                        new UsernamePasswordAuthenticationToken(
                                userDetails, null, userDetails.getAuthorities());

                accessor.setUser(auth);

                System.out.println(">>> [JwtChannelInterceptor] Principal 세팅 완료: " + email);

            } catch (Exception e) {
                throw new IllegalArgumentException("❌ 유효하지 않은 JWT 토큰입니다.", e);
            }
        }
        return message;
    }
}
