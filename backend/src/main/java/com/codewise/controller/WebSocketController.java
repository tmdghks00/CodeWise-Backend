package com.codewise.controller;

import com.codewise.dto.AnalysisResultDto; // AnalysisResultDto 임포트
import com.codewise.dto.WebSocketRequestDto;
import com.codewise.service.AnalysisResultService;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
public class WebSocketController {
    // WebSocket 메시지를 받아 분석 요청을 처리하고 결과를 브로드캐스트하는 컨트롤러

    private final AnalysisResultService analysisResultService;

    public WebSocketController(AnalysisResultService analysisResultService) {
        this.analysisResultService = analysisResultService;
    }

    // "/app/analyze" 경로로 들어오는 STOMP 메시지를 처리 (클라이언트에서 분석 요청)
    // 이 메서드의 반환 값을 "/topic/result" 경로를 구독하는 모든 클라이언트에게 전송
    @MessageMapping("/analyze")
    @SendTo("/topic/result")
    public AnalysisResultDto handleWebSocketAnalysis(WebSocketRequestDto dto) { // 반환 타입을 AnalysisResultDto로 변경
        // AnalysisResultService 를 통해 실시간 코드 분석을 수행하고, 결과를 DB에 저장한 후 DTO를 반환
        // 이제 analyzeAndSaveCodeRealtime 메서드가 DB 저장 로직을 포함합니다.
        return analysisResultService.analyzeAndSaveCodeRealtime(dto.getEmail(), dto.getCode());
    }
}