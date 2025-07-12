package com.codewise.dto;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LoginResponseDto { // 로그인 후 클라이언트에게 JWT 토큰을 응답으로 주기 위한 DTO
    private String token; // 로그인 성공 시 클라이언트에게 전달할 JWT 토큰
}
