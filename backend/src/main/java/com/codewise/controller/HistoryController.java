package com.codewise.controller;

import com.codewise.domain.CodeSubmission;
import com.codewise.domain.User;
import com.codewise.dto.HistoryRequestDto;
import com.codewise.repository.CodeSubmissionRepository;
import com.codewise.repository.UserRepository;
import com.codewise.service.UserService;
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

    private final CodeSubmissionRepository codeSubmissionRepository;
    private final UserRepository userRepository;
    private final UserService userService;

    @GetMapping
    public ResponseEntity<?> getUserHistory(@RequestParam(defaultValue = "id") String sortBy,
                                            @RequestParam(defaultValue = "desc") String direction,
                                            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(
                userService.getSortedUserHistory(userDetails.getUsername(), sortBy, direction)
        );
    }

    @PostMapping
    public ResponseEntity<String> createHistory(@RequestBody HistoryRequestDto dto,
                                                @AuthenticationPrincipal UserDetails userDetails) {
        String email = userDetails.getUsername();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        CodeSubmission submission = CodeSubmission.builder()
                .user(user)
                .language(dto.getLanguage())
                .purpose(dto.getPurpose())
                .errors(dto.getErrors())
                .submittedAt(dto.getCreatedAt() != null ? dto.getCreatedAt() : LocalDateTime.now())
                .build();

        codeSubmissionRepository.save(submission);
        return ResponseEntity.ok("History saved successfully");
    }
}
