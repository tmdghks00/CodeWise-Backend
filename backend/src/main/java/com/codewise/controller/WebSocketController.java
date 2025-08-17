package com.codewise.controller;

import com.codewise.dto.AnalysisResultDto; // AnalysisResultDto 임포트
import com.codewise.dto.WebSocketRequestDto;
import com.codewise.service.AnalysisResultService;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
public class WebSocketController {
    private final AnalysisResultService analysisResultService;
    private final SimpMessagingTemplate messagingTemplate;

    public WebSocketController(AnalysisResultService analysisResultService,
                               SimpMessagingTemplate messagingTemplate) {
        this.analysisResultService = analysisResultService;
        this.messagingTemplate = messagingTemplate;
    }

    @MessageMapping("/analyze")
    public void handleWebSocketAnalysis(WebSocketRequestDto dto, Principal principal) {
        AnalysisResultDto result =
                analysisResultService.analyzeAndSaveCodeRealtime(dto.getEmail(), dto.getCode());

        // 로그인한 사용자별로 결과 전송
        messagingTemplate.convertAndSendToUser(
                principal.getName(),   // 1) 인증된 사용자
                "/queue/result",       // 2) 클라이언트가 구독하는 경로
                result                 // 3) 결과 객체
        );
    }
}