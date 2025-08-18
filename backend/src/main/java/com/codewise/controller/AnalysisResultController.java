package com.codewise.controller;

import com.codewise.domain.AnalysisResult;
import com.codewise.dto.AnalysisResultDto;
import com.codewise.exception.CustomException;
import com.codewise.service.AnalysisResultService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import java.util.List;

@RestController // 이 클래스가 RESTful API 를 제공하는 컨트롤러임을 명시
@RequestMapping("/analysis") // "/analysis" 경로로 들어오는 모든 요청을 처리
public class AnalysisResultController { // 분석 결과 관련 API 요청을 처리하는 컨트롤러

    private final AnalysisResultService analysisResultService;

    public AnalysisResultController(AnalysisResultService analysisResultService) {
        this.analysisResultService = analysisResultService;
    }

    // "/analysis/result/{submissionId}" 경로의 GET 요청 처리 (특정 제출 ID의 분석 결과 조회)
    @GetMapping("/result/{submissionId}")
    public ResponseEntity<AnalysisResultDto> getResult(@PathVariable Long submissionId) {
        return ResponseEntity.ok(analysisResultService.getResultBySubmissionId(submissionId));
    }

    // "/analysis/{id}" 경로의 GET 요청 처리 (특정 ID의 분석 결과 조회)
    @GetMapping("/{id}")
    public ResponseEntity<AnalysisResultDto> getAnalysisById(@PathVariable Long id) {
        return ResponseEntity.ok(analysisResultService.getAnalysisById(id));
    }
}
