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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

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

        String email;

        if (principal instanceof UsernamePasswordAuthenticationToken auth) {
            email = (String) auth.getPrincipal(); // JwtChannelInterceptor에서 세팅한 email
            log.info(">>> [AnalyzeStompController] principal(email) = {}", email);
        } else {
            email = accessor.getSessionId(); // fallback
            log.warn(">>> [AnalyzeStompController] principal is null, fallback sessionId = {}", email);
        }

        aiServerClient.analyze(req).subscribe(result -> {
            try {
                // DB 저장
                analysisResultService.saveNewResult(
                        email,
                        req.code(),
                        req.language(),
                        result
                );
                log.info(">>> [AnalyzeStompController] DB 저장 성공. email={}, lang={}", email, req.language());

                // 결과 전송
                messaging.convertAndSendToUser(email, "/queue/result", result);

            } catch (Exception e) {
                log.error(">>> [AnalyzeStompController] DB 저장 실패. email={}", email, e);
                Map<String, Object> error = Map.of("error", "DB 저장 실패: " + e.getMessage());
                messaging.convertAndSendToUser(email, "/queue/result", error);
            }
        }, err -> {
            log.error(">>> [AnalyzeStompController] AI 서버 호출 실패. email={}, err={}", email, err.getMessage());
            Map<String, Object> error = Map.of("error", err.getMessage());
            messaging.convertAndSendToUser(email, "/queue/result", error);
        });
    }
}
