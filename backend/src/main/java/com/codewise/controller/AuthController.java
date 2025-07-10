package com.codewise.controller;

import com.codewise.dto.LoginRequestDto;
import com.codewise.dto.LoginResponseDto;
import com.codewise.dto.SignupRequestDto;
import com.codewise.service.AuthService;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController // RESTful API 컨트롤러임을 명시
@RequestMapping("/auth") // "/auth" 경로로 들어오는 모든 요청을 이 컨트롤러가 처리
public class AuthController { // 회원가입 및 로그인 등 인증 관련 요청 처리

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/signup") // "/auth/signup" 경로의 POST 요청을 처리 (회원가입)
    public ResponseEntity<String> signup(@RequestBody SignupRequestDto requestDto) {
        authService.signup(requestDto); // AuthService 를 통해 회원가입 로직 수행
        return ResponseEntity.ok("회원가입 성공"); // 성공 시 HTTP 200 OK와 메시지 반환
    }

    @PostMapping("/login") // "/auth/login" 경로의 POST 요청을 처리 (로그인)
    public ResponseEntity<LoginResponseDto> login(@RequestBody LoginRequestDto requestDto) {
        LoginResponseDto responseDto = authService.login(requestDto);
        return ResponseEntity.ok(responseDto); // 성공 시 HTTP 200 OK와 LoginResponseDto 반환
    }
}
