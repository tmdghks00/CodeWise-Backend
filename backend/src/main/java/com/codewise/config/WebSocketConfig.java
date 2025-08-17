package com.codewise.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.*;

@Configuration
@EnableWebSocketMessageBroker  // WebSocket 메시지 브로커 기능을 활성화
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer { // WebSocket 설정을 통해 실시간 통신 구성
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // topic: 브로드캐스트용
        // queue: 사용자 개별 메시징용 (점대점)
        registry.enableSimpleBroker("/topic", "/queue"); // queue 추가
        registry.setApplicationDestinationPrefixes("/app");
    }
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // 클라이언트 연결 엔드포인트
        registry.addEndpoint("/ws").setAllowedOriginPatterns("*").withSockJS();
    }

}
