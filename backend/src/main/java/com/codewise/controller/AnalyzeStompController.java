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
    private final ObjectMapper objectMapper;    // ✅ 핵심 추가

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

        String userKey;

        if (principal != null) {
            userKey = principal.getName(); // JwtChannelInterceptor에서 email을 username으로 넣어줌
            log.info(">>> [AnalyzeStompController] principal(email) = {}", userKey);
        } else {
            userKey = accessor.getSessionId(); // 로그인 안된 사용자 fallback
            log.warn(">>> [AnalyzeStompController] principal is null, fallback sessionId = {}", userKey);
        }

        // ✅ AI Server 호출 (Mono<String> 으로 JSON 문자열 반환)
        aiServerClient.analyze(req).subscribe(aiResponseJson -> {
            try {
                // ✅ JSON 문자열을 JsonNode로 변환 → 필드 누락 방지!
                JsonNode jsonNode = objectMapper.readTree(aiResponseJson);

                // ✅ DB 저장
                analysisResultService.saveNewResult(
                        userKey,
                        req.code(),
                        req.language(),
                        jsonNode.toString()        // JSON 그대로 저장
                );

                log.info(">>> [AnalyzeStompController] DB 저장 성공 userKey={}, lang={}", userKey, req.language());

                // ✅ WebSocket으로 JsonNode 그대로 전송
                if (principal != null) {
                    messaging.convertAndSendToUser(userKey, "/queue/result", jsonNode);
                } else {
                    messaging.convertAndSendToUser(userKey, "/queue/result", jsonNode, headersForSession(userKey));
                }

            } catch (Exception e) {
                log.error(">>> [AnalyzeStompController] DB 저장 or JSON parsing 실패 userKey={}", userKey, e);

                Map<String, Object> error = Map.of("error", "DB 저장 실패 또는 JSON 변환 실패");

                if (principal != null) {
                    messaging.convertAndSendToUser(userKey, "/queue/result", error);
                } else {
                    messaging.convertAndSendToUser(userKey, "/queue/result", error, headersForSession(userKey));
                }
            }
        }, err -> {
            log.error(">>> [AnalyzeStompController] AI 서버 호출 실패 userKey={}, err={}", userKey, err.getMessage());

            Map<String, Object> error = Map.of("error", err.getMessage());
            if (principal != null) {
                messaging.convertAndSendToUser(userKey, "/queue/result", error);
            } else {
                messaging.convertAndSendToUser(userKey, "/queue/result", error, headersForSession(userKey));
            }
        });
    }
}
