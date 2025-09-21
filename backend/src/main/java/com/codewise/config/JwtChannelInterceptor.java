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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
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
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            log.info("[JwtChannelInterceptor] WebSocket CONNECT 요청 감지");

            String token = accessor.getFirstNativeHeader("Authorization");
            if (token == null || !token.startsWith("Bearer ")) {
                log.error("JWT 토큰이 누락되었거나 잘못된 형식");
                throw new IllegalArgumentException("JWT 토큰이 누락되었거나 잘못된 형식입니다.");
            }

            token = token.substring(7);
            log.info("추출된 JWT 토큰: {}", token);

            try {
                // JWT 에서 email 추출
                String email = jwtUtil.getEmail(token);
                log.info("[JwtChannelInterceptor] JWT 에서 추출된 email = {}", email);

                // DB 사용자 존재 여부 확인
                boolean exists = userRepository.existsByEmail(email);
                log.info("DB 사용자 존재 여부(email={}): {}", email, exists);

                if (!exists) {
                    log.warn("DB에 사용자 없음 → 자동 등록 진행: {}", email);

                    User newUser = new User();
                    newUser.setEmail(email);

                    String rawPassword = "test1234";
                    String encodedPassword = new BCryptPasswordEncoder().encode(rawPassword);
                    newUser.setPassword(encodedPassword);

                    newUser.setRole(UserRole.USER);

                    User savedUser = userRepository.save(newUser);
                    log.info("새 사용자 자동 등록 완료: id={}, email={}", savedUser.getId(), savedUser.getEmail());
                }

                // DB 에서 사용자 정보 로드
                UserDetails userDetails = userDetailsService.loadUserByUsername(email);
                log.info("DB 에서 UserDetails 로드 성공: {}", userDetails.getUsername());

                // Authentication 객체 세팅
                UsernamePasswordAuthenticationToken auth =
                        new UsernamePasswordAuthenticationToken(
                                email, null, userDetails.getAuthorities());

                accessor.setUser(auth);
                log.info("Principal 세팅 완료: {}", auth.getName());

            } catch (Exception e) {
                log.error("JWT 검증 실패 또는 사용자 정보 로드 실패", e);
                throw new IllegalArgumentException("유효하지 않은 JWT 토큰입니다.", e);
            }
        }
        return message;
    }
}