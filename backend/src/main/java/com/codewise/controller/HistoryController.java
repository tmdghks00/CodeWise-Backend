package com.codewise.controller;

import com.codewise.domain.AnalysisHistory;
import com.codewise.domain.User;
import com.codewise.dto.HistoryRequestDto;
import com.codewise.repository.AnalysisHistoryRepository;
import com.codewise.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/user/history")
@RequiredArgsConstructor
public class HistoryController {

    private final AnalysisHistoryRepository analysisHistoryRepository;
    private final UserRepository userRepository;

    @PostMapping
    public ResponseEntity<String> saveHistory(
            @RequestBody HistoryRequestDto dto,
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestHeader(value = "X-Idempotency-Key", required = false) String idemKey
    ) {
        String email = userDetails.getUsername();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        LocalDateTime createdAt = dto.getCreatedAt() != null ? dto.getCreatedAt() : LocalDateTime.now();

        // IdempotencyKey 중복 방지 (같은 키 중복 insert 방지)
        if (idemKey != null && analysisHistoryRepository.existsByUserAndIdempotencyKey(user, idemKey)) {
            return ResponseEntity.ok("Duplicate request ignored");
        }

        // 에러 목록이 비었을 경우에도 최소 1건 저장
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
            AnalysisHistory h = AnalysisHistory.builder()
                    .user(user)
                    .language(dto.getLanguage())
                    .purpose(dto.getPurpose())
                    .errorType(null)
                    .errorMessage(null)
                    .createdAt(createdAt)
                    .idempotencyKey(idemKey != null ? idemKey : UUID.randomUUID().toString())
                    .build();
            analysisHistoryRepository.save(h);
        }

        return ResponseEntity.ok("History saved successfully");
    }

    @GetMapping
    public ResponseEntity<List<AnalysisHistory>> getUserHistory(@AuthenticationPrincipal UserDetails userDetails) {
        String email = userDetails.getUsername();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        List<AnalysisHistory> historyList = analysisHistoryRepository.findByUser(user);
        return ResponseEntity.ok(historyList);
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getUserStats(org.springframework.security.core.Authentication authentication) {
        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        List<AnalysisHistory> historyList = analysisHistoryRepository.findByUser(user);

        Map<String, Long> langCount = historyList.stream()
                .filter(h -> h.getLanguage() != null && !h.getLanguage().isEmpty())
                .collect(Collectors.groupingBy(AnalysisHistory::getLanguage, Collectors.counting()));

        Map<String, Long> purposeCount = historyList.stream()
                .filter(h -> h.getPurpose() != null && !h.getPurpose().isEmpty())
                .collect(Collectors.groupingBy(AnalysisHistory::getPurpose, Collectors.counting()));

        Map<String, Long> errorCount = historyList.stream()
                .filter(h -> h.getErrorMessage() != null && !h.getErrorMessage().isEmpty())
                .collect(Collectors.groupingBy(AnalysisHistory::getErrorMessage, Collectors.counting()));

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
