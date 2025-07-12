package com.codewise.dto;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SignupRequestDto { // 회원가입 시 사용자의 이메일, 비밀번호를 클라이언트로부터 전달받는 DTO 클래스
    private String email; // 회원가입할 사용자 이메일
    private String password; // 회원가입할 사용자 비밀번호
}
