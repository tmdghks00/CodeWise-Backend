package com.codewise.controller;

import com.codewise.domain.AnalysisHistory;
import com.codewise.domain.User;
import com.codewise.dto.HistoryRequestDto;
import com.codewise.repository.AnalysisHistoryRepository;
import com.codewise.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 변경 요약
 * 1. 이메일 기반 사용자 조회 확실히 통일
 * 2. idempotencyKey 중복 방지 로직 유지
 * 3. 빈 errors 배열 처리 로직 명확화
 * 4. 마이페이지 통계 API(getUserStats) 출력 정리
 */
@RestController
@RequestMapping("/user/history")
@RequiredArgsConstructor
public class HistoryController {

    private final AnalysisHistoryRepository analysisHistoryRepository;
    private final UserRepository userRepository;

    /**
     * 사용자 히스토리 저장
     */
    @PostMapping
    @Transactional
    public ResponseEntity<String> saveHistory(
            @RequestBody HistoryRequestDto dto,
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestHeader(value = "X-Idempotency-Key", required = false) String idemKey
    ) {
        String email = userDetails.getUsername();

        // 이메일 기반 사용자 조회 (랜덤 생성 방지)
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        LocalDateTime createdAt = dto.getCreatedAt() != null ? dto.getCreatedAt() : LocalDateTime.now();

        // 중복 방지 (같은 키는 무시)
        if (idemKey != null && analysisHistoryRepository.existsByUserAndIdempotencyKey(user, idemKey)) {
            return ResponseEntity.ok("Duplicate request ignored");
        }

        // 에러 리스트 처리
        List<HistoryRequestDto.ErrorInfo> errors = dto.getErrors();
        if (errors != null && !errors.isEmpty()) {
            for (HistoryRequestDto.ErrorInfo e : errors) {
                AnalysisHistory h = AnalysisHistory.builder()
                        .user(user)
                        .language(dto.getLanguage())
                        .purpose(dto.getPurpose())
                        .errorType(e.getType())
                        .errorMessage(e.getMessage())
                        .createdAt(createdAt)
                        .idempotencyKey(idemKey != null ? idemKey : UUID.randomUUID().toString())
                        .build();
                analysisHistoryRepository.save(h);
            }
        } else {
            // errors가 비어 있을 때 최소 1건 저장
            AnalysisHistory h = AnalysisHistory.builder()
                    .user(user)
                    .language(dto.getLanguage())
                    .purpose(dto.getPurpose())
                    .createdAt(createdAt)
                    .idempotencyKey(idemKey != null ? idemKey : UUID.randomUUID().toString())
                    .build();
            analysisHistoryRepository.save(h);
        }

        return ResponseEntity.ok("✅ History saved successfully");
    }

    /**
     * 사용자별 전체 히스토리 조회
     */
    @GetMapping
    public ResponseEntity<List<AnalysisHistory>> getUserHistory(@AuthenticationPrincipal UserDetails userDetails) {
        String email = userDetails.getUsername();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        List<AnalysisHistory> historyList = analysisHistoryRepository.findByUser(user);
        return ResponseEntity.ok(historyList);
    }

    /**
     * 사용자 히스토리 통계 (언어/목적/오류 TOP)
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getUserStats(@AuthenticationPrincipal UserDetails userDetails) {
        String email = userDetails.getUsername();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        List<AnalysisHistory> historyList = analysisHistoryRepository.findByUser(user);

        // 언어별 통계
        Map<String, Long> langCount = historyList.stream()
                .filter(h -> h.getLanguage() != null && !h.getLanguage().isEmpty())
                .collect(Collectors.groupingBy(AnalysisHistory::getLanguage, Collectors.counting()));

        // 목적별 통계
        Map<String, Long> purposeCount = historyList.stream()
                .filter(h -> h.getPurpose() != null && !h.getPurpose().isEmpty())
                .collect(Collectors.groupingBy(AnalysisHistory::getPurpose, Collectors.counting()));

        // 오류별 통계
        Map<String, Long> errorCount = historyList.stream()
                .filter(h -> h.getErrorMessage() != null && !h.getErrorMessage().isEmpty())
                .collect(Collectors.groupingBy(AnalysisHistory::getErrorMessage, Collectors.counting()));

        // 정렬 및 상위 5개 제한
        List<Map<String, Object>> topLanguages = langCount.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(5)
                .map(e -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("language", e.getKey());
                    m.put("count", e.getValue());
                    return m;
                })
                .collect(Collectors.toList());

        List<Map<String, Object>> topPurposes = purposeCount.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(5)
                .map(e -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("purpose", e.getKey());
                    m.put("count", e.getValue());
                    return m;
                })
                .collect(Collectors.toList());

        List<Map<String, Object>> topErrors = errorCount.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(10)
                .map(e -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("error", e.getKey());
                    m.put("count", e.getValue());
                    return m;
                })
                .collect(Collectors.toList());


        Map<String, Object> stats = new HashMap<>();
        stats.put("topLanguages", topLanguages);
        stats.put("topPurposes", topPurposes);
        stats.put("topErrors", topErrors);

        return ResponseEntity.ok(stats);
    }
}
