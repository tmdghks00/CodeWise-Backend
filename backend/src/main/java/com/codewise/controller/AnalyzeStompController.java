package com.codewise.controller;

import com.codewise.dto.AnalyzeRequest;
import com.codewise.service.AiServerClient;
import com.codewise.service.AnalysisResultService;
import lombok.RequiredArgsConstructor;
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

    private final AiServerClient aiServerClient;
    private final SimpMessagingTemplate messaging;
    private final AnalysisResultService analysisResultService;

    // 세션ID 기반 메시지 헤더 생성 (principal이 null일 때 사용)
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

        // principal이 있으면 이메일, 없으면 세션ID를 사용
        String userKey = (principal != null) ? principal.getName()
                : accessor.getSessionId();

        aiServerClient.analyze(req).subscribe(result -> {
            try {
                // DB 저장
                analysisResultService.saveNewResult(
                        userKey,
                        req.code(),
                        req.language(),
                        result
                );

                // 결과 전송
                if (principal != null) {
                    messaging.convertAndSendToUser(userKey, "/queue/result", result);
                } else {
                    messaging.convertAndSendToUser(
                            userKey, "/queue/result", result, headersForSession(userKey));
                }

            } catch (Exception e) {
                Map<String, Object> error = Map.of("error", "DB 저장 실패: " + e.getMessage());
                if (principal != null) {
                    messaging.convertAndSendToUser(userKey, "/queue/result", error);
                } else {
                    messaging.convertAndSendToUser(
                            userKey, "/queue/result", error, headersForSession(userKey));
                }
            }
        }, err -> {
            Map<String, Object> error = Map.of("error", err.getMessage());
            if (principal != null) {
                messaging.convertAndSendToUser(userKey, "/queue/result", error);
            } else {
                messaging.convertAndSendToUser(
                        userKey, "/queue/result", error, headersForSession(userKey));
            }
        });
    }
}
