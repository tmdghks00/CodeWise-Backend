package com.codewise.config;

import com.codewise.domain.User;
import com.codewise.domain.UserRole;
import com.codewise.repository.UserRepository;
import com.codewise.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Component
@RequiredArgsConstructor
public class JwtChannelInterceptor implements ChannelInterceptor {

    private static final Logger log = LoggerFactory.getLogger(JwtChannelInterceptor.class);

    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;
    private final UserRepository userRepository;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {

        // wrap() → MessageHeaderAccessor.getAccessor() 로 변경
        StompHeaderAccessor accessor =
                MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            log.info("[JwtChannelInterceptor] WebSocket CONNECT 요청 감지");

            String token = accessor.getFirstNativeHeader("Authorization");
            if (token == null || !token.startsWith("Bearer ")) {
                log.error("JWT 토큰이 누락되었거나 잘못된 형식");
                throw new IllegalArgumentException("JWT 토큰이 누락되었거나 잘못된 형식입니다.");
            }

            token = token.substring(7);
            log.info("추출된 JWT 토큰: {}", token);

            try {
                String email = jwtUtil.getEmail(token);
                log.info("[JwtChannelInterceptor] JWT 에서 추출된 email = {}", email);

                if (!userRepository.existsByEmail(email)) {
                    log.warn("⚠️ DB에 사용자 없음 → 회원가입 없이 WebSocket 사용 불가");
                    throw new IllegalArgumentException("등록되지 않은 사용자입니다.");
                }

                UserDetails userDetails = userDetailsService.loadUserByUsername(email);
                UsernamePasswordAuthenticationToken auth =
                        new UsernamePasswordAuthenticationToken(
                                userDetails, null, userDetails.getAuthorities());

                // STOMP 세션에 Principal 저장
                accessor.setUser(auth);

                // SecurityContext 에도 Principal 저장
                SecurityContextHolder.getContext().setAuthentication(auth);

                log.info(" Principal 세팅 완료: {}", auth.getName());

            } catch (Exception e) {
                log.error("JWT 검증 실패 또는 사용자 정보 로드 실패", e);
                throw new IllegalArgumentException("유효하지 않은 JWT 토큰입니다.", e);
            }
        }

        return message;
    }
}
