package com.codewise.controller;

import com.codewise.dto.AnalyzeRequest;
import com.codewise.dto.AnalyzeResponse;
import com.codewise.service.AiServerClient;
import com.codewise.service.AnalysisResultService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
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

    @MessageMapping("/analyze")
    public void receive(AnalyzeRequest req, Principal principal) {
        // principal 객체를 통해 현재 로그인한 사용자 정보를 가져옵니다.
        String userEmail = principal.getName();

        aiServerClient.analyze(req).subscribe(result -> {
            try {
                // 1. AnalysisResultService에서 새 제출로 저장
                // saveNewResult 메서드가 존재한다고 가정하고, 사용자 이메일, 코드, 언어, AI 응답을 전달
                analysisResultService.saveNewResult(
                        userEmail,
                        req.code(),
                        req.language(),
                        result
                );

                // 2. 특정 사용자에게만 분석 결과를 전송합니다.
                messaging.convertAndSendToUser(
                        userEmail,
                        "/queue/result",
                        result
                );

            } catch (Exception e) {
                // DB 저장 중 오류 발생 시 에러 메시지를 특정 사용자에게만 전송
                messaging.convertAndSendToUser(
                        userEmail,
                        "/queue/result",
                        Map.of("error", "DB 저장 실패: " + e.getMessage())
                );
            }
        }, err -> {
            // AI 응답 실패 시: 에러를 특정 사용자에게만 전송
            Map<String, Object> error = Map.of(
                    "error", err.getMessage()
            );
            messaging.convertAndSendToUser(
                    userEmail,
                    "/queue/result",
                    error
            );
        });
    }
}