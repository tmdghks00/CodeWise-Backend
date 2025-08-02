package com.codewise.service;

import com.fasterxml.jackson.databind.JsonNode; // JSON 파싱을 위한 JsonNode 임포트
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class AiServerClient {
    // 외부 AI 서버와 통신하여 코드 분석 요청을 보내고 결과를 받아오는 역할을 하는 클래스
    private final String aiServerUrl = "http://localhost:5000/analyze_code"; // AI 서버의 엔드포인트 URL

    // AI 서버에 코드 분석 요청을 보내고, JsonNode 형태로 응답을 받아오는 메서드
    public JsonNode analyzeCode(String username, String code) {
        RestTemplate restTemplate = new RestTemplate(); // RestTemplate 인스턴스 생성 (HTTP 통신을 위함)
        Map<String, String> request = new HashMap<>(); // 요청 본문에 담을 데이터를 위한 Map 생성
        request.put("username", username); // 요청 데이터에 사용자 이름 추가
        request.put("code", code); // 요청 데이터에 코드 내용 추가

        HttpHeaders headers = new HttpHeaders(); // HTTP 헤더 설정 객체 생성
        headers.setContentType(MediaType.APPLICATION_JSON); // Content-Type 을 JSON 으로 설정

        HttpEntity<Map<String, String>> entity = new HttpEntity<>(request, headers);

        ResponseEntity<JsonNode> response = restTemplate.postForEntity(aiServerUrl, entity, JsonNode.class);

        if (response.getStatusCode().is2xxSuccessful()) {
            return response.getBody();
        } else {
            throw new RuntimeException("AI 서버 분석 요청 실패: " + response.getStatusCode() + " - " + response.getBody());
        }
    }
}