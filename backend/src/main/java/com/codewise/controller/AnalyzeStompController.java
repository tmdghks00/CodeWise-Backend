package com.codewise.controller;

import com.codewise.dto.AnalyzeRequest;
import com.codewise.service.AiServerClient;
import com.codewise.service.AnalysisResultService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.Map;

@Slf4j
@Controller
@RequiredArgsConstructor
public class AnalyzeStompController {

    private final AiServerClient aiServerClient;
    private final SimpMessagingTemplate messaging;
    private final AnalysisResultService analysisResultService;
    private final ObjectMapper objectMapper;

    // 세션에 WebSocket 메시지를 보내기 위한 Header 설정
    private org.springframework.messaging.MessageHeaders headersForSession(String sessionId) {
        SimpMessageHeaderAccessor accessor =
                SimpMessageHeaderAccessor.create(SimpMessageType.MESSAGE);
        accessor.setSessionId(sessionId);
        accessor.setLeaveMutable(true);
        return accessor.getMessageHeaders();
    }

    @MessageMapping("/analyze")   // ✅ WebSocket 메시지 받는 엔드포인트
    public void receive(AnalyzeRequest req,
                        Principal principal,
                        SimpMessageHeaderAccessor accessor) {

        // ✅ userKey 는 무조건 email (principal.name = email)
        String email;
        if (principal != null) {
            email = principal.getName();   // ✅ JwtChannelInterceptor 에서 email 넣어줌
            log.info(">>> [AnalyzeStompController] Email from principal = {}", email);
        } else {
            // 로그인 안 했을 경우 세션 id 를 key 로 사용
            email = accessor.getSessionId();
            log.warn(">>> [AnalyzeStompController] principal is NULL -> fallback sessionId = {}", email);
        }

        // ✅ AI Server 호출 (Mono<String> 으로 JSON 문자열 반환)
        aiServerClient.analyze(req).subscribe(aiResponseJson -> {
            try {
                JsonNode jsonNode = objectMapper.readTree(aiResponseJson);

                // ✅ DB 저장 (Stomp + WebSocket)
                analysisResultService.saveNewResult(
                        email,
                        req.code(),
                        req.language(),
                        jsonNode.toString()
                );

                log.info("✅ 분석 결과 저장 완료 (email={}, lang={})", email, req.language());

                // ✅ WebSocket 메시지 전송
                if (principal != null) {
                    messaging.convertAndSendToUser(email, "/queue/result", jsonNode);
                } else {
                    messaging.convertAndSendToUser(email, "/queue/result", jsonNode, headersForSession(email));
                }

            } catch (Exception e) {
                log.error("❌ DB 저장 또는 JSON 변환 실패 (email={})", email, e);

                Map<String, Object> error = Map.of("error", "DB 저장 실패 또는 JSON 변환 오류");
                if (principal != null) {
                    messaging.convertAndSendToUser(email, "/queue/result", error);
                } else {
                    messaging.convertAndSendToUser(email, "/queue/result", error, headersForSession(email));
                }
            }
        }, err -> {
            log.error("🚨 AI 서버 요청 실패 email={}, err={}", email, err.getMessage());

            Map<String, Object> error = Map.of("error", err.getMessage());
            if (principal != null) {
                messaging.convertAndSendToUser(email, "/queue/result", error);
            } else {
                messaging.convertAndSendToUser(email, "/queue/result", error, headersForSession(email));
            }
        });
    }
}
