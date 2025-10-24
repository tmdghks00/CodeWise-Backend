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

@RestController
@RequestMapping("/user/history")
@RequiredArgsConstructor
public class HistoryController {

    private final AnalysisHistoryRepository analysisHistoryRepository;
    private final UserRepository userRepository;

    /**
     * STOMP 결과 수신 시 프론트에서 saveAnalysisHistory()로 호출됨
     */
    @PostMapping
    public ResponseEntity<String> saveHistory(
            @RequestBody HistoryRequestDto dto,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        String email = userDetails.getUsername();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        LocalDateTime now = LocalDateTime.now();

        // errors 배열 각각 insert
        if (dto.getErrors() != null && !dto.getErrors().isEmpty()) {
            dto.getErrors().forEach(err -> {
                AnalysisHistory history = AnalysisHistory.builder()
                        .user(user)
                        .language(dto.getLanguage())
                        .purpose(dto.getPurpose())
                        .errorType(err.getType())
                        .errorMessage(err.getMessage())
                        .createdAt(dto.getCreatedAt() != null ? dto.getCreatedAt() : now)
                        .build();
                analysisHistoryRepository.save(history);
            });
        } else {
            // errors가 비어있어도 기본 1건 저장
            AnalysisHistory history = AnalysisHistory.builder()
                    .user(user)
                    .language(dto.getLanguage())
                    .purpose(dto.getPurpose())
                    .errorType(null)
                    .errorMessage(null)
                    .createdAt(dto.getCreatedAt() != null ? dto.getCreatedAt() : now)
                    .build();
            analysisHistoryRepository.save(history);
        }

        return ResponseEntity.ok("History saved successfully");
    }
}
