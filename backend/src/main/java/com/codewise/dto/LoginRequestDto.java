package com.codewise.dto;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginRequestDto { // 로그인 시 클라이언트가 보내는 이메일과 비밀번호 정보를 담는 DTO
    private String email; // 사용자 로그인 이메일
    private String password; // 사용자 비밀번호
}
