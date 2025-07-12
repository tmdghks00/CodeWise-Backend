package com.codewise.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WebSocketRequestDto { // WebSocket 을 통해 실시간 분석 요청을 보낼 때 클라이언트가 전달하는 데이터를 담는 DTO 클래스
    private String email;     // 코드 분석을 요청하는 사용자의 이메일
    private String code;      // 실시간으로 분석 요청할 코드 내용
    private String language;  // 제출된 코드의 프로그래밍 언어
    private String sessionId; // WebSocket 연결 세션 ID
}
