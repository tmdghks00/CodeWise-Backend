package com.codewise.service;

import com.codewise.domain.User;
import com.codewise.domain.UserRole;
import com.codewise.dto.LoginRequestDto;
import com.codewise.dto.LoginResponseDto;
import com.codewise.dto.SignupRequestDto;
import com.codewise.repository.UserRepository;
import com.codewise.util.JwtUtil;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
// 사용자 회원가입 및 로그인 처리 로직을 담당하는 서비스 클래스

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final BCryptPasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    public void signup(SignupRequestDto dto) { // 회원가입 로직을 수행하는 메서드
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException("이미 존재하는 사용자입니다.");
        }
        User user = User.builder()
                .email(dto.getEmail())
                .password(passwordEncoder.encode(dto.getPassword()))
                .role(UserRole.USER)
                .build();
        userRepository.save(user);
    }

    public LoginResponseDto login(LoginRequestDto dto) { // 로그인 로직을 수행하는 메서드
        User user = userRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("사용자 없음"));
        // 제출된 비밀번호와 저장된 암호화된 비밀번호 비교
        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("비밀번호 불일치");
        }
        String token = jwtUtil.generateToken(user.getEmail()); // 사용자 이메일로 JWT 토큰 생성
        return new LoginResponseDto(token); // 생성된 토큰을 포함한 응답 DTO 반환
    }
}
