package com.codewise.util;

import io.jsonwebtoken.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value; // @Value 어노테이션을 위해 필요

import java.util.Date;

@Component
public class JwtUtil {
    // JWT 토큰 생성, 검증 및 사용자 정보 추출 기능을 제공하는 유틸 클래스

    // @Value 어노테이션을 사용하여 application.properties/yml에서 jwt.secret.key 값을 주입받습니다.
    private final String secretKey; // <-- 초기값을 제거함

    // final 필드이므로 생성자를 통해 주입받음
    public JwtUtil(@Value("${jwt.secret.key}") String secretKey) {
        this.secretKey = secretKey;
    }

    private final long tokenValidity = 1000 * 60 * 60 * 24; // 24시간 => 토큰 유효 시간 설정

    public String generateToken(String username) { // 사용자 이름을 기반으로 JWT 토큰을 생성하는 메서드
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + tokenValidity))
                .signWith(SignatureAlgorithm.HS256, secretKey)
                .compact();
    }

    public boolean validateToken(String token) { // 주어진 JWT 토큰의 유효성을 검증하는 메서드
        try {
            Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public String getUsername(String token) { // 주어진 JWT 토큰에서 사용자 이름을 추출하는 메서드
        return Jwts.parser()
                .setSigningKey(secretKey)
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    public String resolveToken(HttpServletRequest request) { // HTTP 요청 헤더에서 "Bearer" 토큰을 추출하는 메서드
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

}