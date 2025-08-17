package com.codewise.controller;

import com.codewise.dto.CodeSubmissionDto;
import com.codewise.service.CodeSubmissionService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/code") // "/code" 경로로 들어오는 모든 요청을 처리
public class CodeSubmissionController { // 코드 제출 관련 요청을 처리하는 컨트롤러

    private final CodeSubmissionService codeSubmissionService;
    public CodeSubmissionController(CodeSubmissionService codeSubmissionService) {
        this.codeSubmissionService = codeSubmissionService;
    }

    // "/code/submit" 경로의 POST 요청을 처리 (코드 제출)
    @PostMapping("/submit")
    public ResponseEntity<String> submitCode(@AuthenticationPrincipal String username,
                                             @RequestBody CodeSubmissionDto dto) {
        codeSubmissionService.submitCode(username, dto);
        return ResponseEntity.ok("코드 제출 완료");
    }

    // "/code/list" 경로의 GET 요청을 처리 (현재 사용자가 제출한 코드 목록 조회)
    @GetMapping("/list")
    public ResponseEntity<List<CodeSubmissionDto>> getMyCodeList(@AuthenticationPrincipal String username) {
        return ResponseEntity.ok(codeSubmissionService.getUserCodeList(username));
    }

    // "/code/{id}" 경로의 GET 요청을 처리 (특정 ID의 코드 조회)
    @GetMapping("/{id}")
    public ResponseEntity<CodeSubmissionDto> getCodeById(@PathVariable Long id) {
        return ResponseEntity.ok(codeSubmissionService.getCodeById(id));
    }

    // "/code/{id}" 경로의 DELETE 요청을 처리 (특정 ID의 코드 삭제)
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteCode(@PathVariable Long id) {
        codeSubmissionService.deleteCode(id);
        return ResponseEntity.ok("코드 삭제 완료");
    }

    // "/code/submission/user" 경로의 GET 요청 처리 (현재 로그인한 사용자의 제출 내역 조회)
    @GetMapping("/submission/user")
    public ResponseEntity<List<CodeSubmissionDto>> getUserSubmissions(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(codeSubmissionService.getSubmissionsByUser(userDetails.getUsername()));
    }

}
