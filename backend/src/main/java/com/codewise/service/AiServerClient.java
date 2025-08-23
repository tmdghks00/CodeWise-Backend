package com.codewise.service;

import com.codewise.dto.AnalyzeRequest;
import com.codewise.dto.AnalyzeResponse;
import org.springframework.beans.factory.annotation.Value; // @Value 어노테이션을 위해 필요
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class AiServerClient {

    private final WebClient webClient;

    // 생성자를 통해 'ai.server.url' 값을 주입받도록 수정합니다.
    public AiServerClient(@Value("${ai.server.url}") String aiServerUrl) {
        this.webClient = WebClient.builder()
                .baseUrl(aiServerUrl) // 주입받은 URL을 사용
                .build();
    }

    // AI 서버 호출 (비동기, 리액티브 방식)
    public Mono<AnalyzeResponse> analyze(AnalyzeRequest req) {
        return webClient.post()
                .uri("/analyze") // AI 서버 엔드포인트
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(req)
                .retrieve()
                .onStatus(status -> status.isError(),
                        res -> res.bodyToMono(String.class)
                                .map(msg -> new RuntimeException("AI error: " + msg)))
                .bodyToMono(AnalyzeResponse.class);
    }
}
