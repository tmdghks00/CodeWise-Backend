package com.codewise.controller;

import com.codewise.dto.AnalysisResultDto;
import com.codewise.service.AnalysisResultService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/analysis")
public class AnalysisResultController {

    private final AnalysisResultService analysisResultService;

    public AnalysisResultController(AnalysisResultService analysisResultService) {
        this.analysisResultService = analysisResultService;
    }

    @GetMapping("/result/{submissionId}")
    public ResponseEntity<AnalysisResultDto> getResult(@PathVariable Long submissionId) {
        return ResponseEntity.ok(analysisResultService.getResultBySubmissionId(submissionId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AnalysisResultDto> getAnalysisById(@PathVariable Long id) {
        return ResponseEntity.ok(analysisResultService.getAnalysisById(id));
    }

    // submissionId + userId 조합
    @GetMapping("/result/{submissionId}/user/{userId}")
    public ResponseEntity<AnalysisResultDto> getResultForUser(
            @PathVariable Long submissionId,
            @PathVariable Long userId) {
        return ResponseEntity.ok(
                analysisResultService.getResultBySubmissionIdAndUserId(submissionId, userId)
        );
    }
}
