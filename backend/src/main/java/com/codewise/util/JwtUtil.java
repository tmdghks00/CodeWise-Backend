package com.codewise.util;

import io.jsonwebtoken.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;

import java.util.Date;

@Component
public class JwtUtil {
    // JWT 토큰 생성, 검증 및 사용자 정보 추출 기능 제공

    private final String secretKey;

    public JwtUtil(@Value("${jwt.secret.key}") String secretKey) {
        this.secretKey = secretKey;
    }

    private final long tokenValidity = 1000 * 60 * 60 * 24; // 24시간

    // JWT 토큰 생성 (email을 subject로 저장)
    public String generateToken(String email) {
        return Jwts.builder()
                .setSubject(email)
                .claim("email", email) // email 클레임도 함께 저장
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + tokenValidity))
                .signWith(SignatureAlgorithm.HS256, secretKey)
                .compact();
    }

    // 토큰 유효성 검증
    public boolean validateToken(String token) {
        try {
            Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    // 토큰에서 email 꺼내기 (email 필드 우선, 없으면 sub 사용)
    public String getEmail(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(secretKey)
                .parseClaimsJws(token)
                .getBody();

        String email = claims.get("email", String.class);
        return (email != null) ? email : claims.getSubject();
    }

    // HTTP 요청에서 Bearer 토큰 추출
    public String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
