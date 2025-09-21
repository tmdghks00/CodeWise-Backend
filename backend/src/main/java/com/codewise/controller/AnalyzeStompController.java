package com.codewise.controller;

import com.codewise.dto.AnalyzeRequest;
import com.codewise.service.AiServerClient;
import com.codewise.service.AnalysisResultService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class AnalyzeStompController {

    private static final Logger log = LoggerFactory.getLogger(AnalyzeStompController.class);

    private final AiServerClient aiServerClient;
    private final SimpMessagingTemplate messaging;
    private final AnalysisResultService analysisResultService;

    private org.springframework.messaging.MessageHeaders headersForSession(String sessionId) {
        SimpMessageHeaderAccessor accessor =
                SimpMessageHeaderAccessor.create(SimpMessageType.MESSAGE);
        accessor.setSessionId(sessionId);
        accessor.setLeaveMutable(true);
        return accessor.getMessageHeaders();
    }

    @MessageMapping("/analyze")
    public void receive(AnalyzeRequest req,
                        Principal principal,
                        SimpMessageHeaderAccessor accessor) {

        String userKey;

        if (principal != null) {
            userKey = principal.getName(); // JwtChannelInterceptor 에서 email 세팅됨
            log.info(">>> [AnalyzeStompController] principal(email) = {}", userKey);
        } else {
            userKey = accessor.getSessionId();
            log.warn(">>> [AnalyzeStompController] principal is null, fallback sessionId = {}", userKey);
        }

        aiServerClient.analyze(req).subscribe(result -> {
            try {
                // DB 저장 시도
                analysisResultService.saveNewResult(
                        userKey,
                        req.code(),
                        req.language(),
                        result
                );
                log.info(">>> [AnalyzeStompController] DB 저장 성공. userKey={}, lang={}", userKey, req.language());

                // 결과 전송
                if (principal != null) {
                    messaging.convertAndSendToUser(userKey, "/queue/result", result);
                } else {
                    messaging.convertAndSendToUser(userKey, "/queue/result", result, headersForSession(userKey));
                }

            } catch (Exception e) {
                log.error(">>> [AnalyzeStompController] DB 저장 실패. userKey={}", userKey, e);
                Map<String, Object> error = Map.of("error", "DB 저장 실패: " + e.getMessage());
                if (principal != null) {
                    messaging.convertAndSendToUser(userKey, "/queue/result", error);
                } else {
                    messaging.convertAndSendToUser(userKey, "/queue/result", error, headersForSession(userKey));
                }
            }
        }, err -> {
            log.error(">>> [AnalyzeStompController] AI 서버 호출 실패. userKey={}, err={}", userKey, err.getMessage());
            Map<String, Object> error = Map.of("error", err.getMessage());
            if (principal != null) {
                messaging.convertAndSendToUser(userKey, "/queue/result", error);
            } else {
                messaging.convertAndSendToUser(userKey, "/queue/result", error, headersForSession(userKey));
            }
        });
    }
}