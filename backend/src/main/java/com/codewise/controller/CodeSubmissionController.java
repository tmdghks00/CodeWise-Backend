package com.codewise.controller;

import com.codewise.dto.CodeSubmissionDto;
import com.codewise.service.CodeSubmissionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/code")
public class CodeSubmissionController {

    private final CodeSubmissionService codeSubmissionService;

    public CodeSubmissionController(CodeSubmissionService codeSubmissionService) {
        this.codeSubmissionService = codeSubmissionService;
    }

    // userId 포함해서 코드 제출
    @PostMapping("/submit")
    public ResponseEntity<String> submitCode(@RequestBody CodeSubmissionDto dto) {
        codeSubmissionService.submitCode(dto.getUserId(), dto);
        return ResponseEntity.ok("코드 제출 완료");
    }

    @GetMapping("/{id}")
    public ResponseEntity<CodeSubmissionDto> getCodeById(@PathVariable Long id) {
        return ResponseEntity.ok(codeSubmissionService.getCodeById(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteCode(@PathVariable Long id) {
        codeSubmissionService.deleteCode(id);
        return ResponseEntity.ok("코드 삭제 완료");
    }

    @GetMapping("/submission/user/{userId}")
    public ResponseEntity<List<CodeSubmissionDto>> getUserSubmissions(@PathVariable Long userId) {
        return ResponseEntity.ok(codeSubmissionService.getSubmissionsByUserId(userId));
    }
}
