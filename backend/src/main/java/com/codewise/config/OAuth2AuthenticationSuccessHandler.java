package com.codewise.config;

import com.codewise.util.JwtUtil;
import com.codewise.service.CustomOAuth2User; // CustomOAuth2User 임포트
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler; // 이 부분 수정됨
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

@Component
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtUtil jwtUtil;
    // 프론트엔드 리다이렉트 URL (개발 환경에 맞게 수정)
    private final String frontendRedirectUrl = "https://codewise-frontend.vercel.app/oauth2/redirect";

    public OAuth2AuthenticationSuccessHandler(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String email = oAuth2User instanceof CustomOAuth2User ? ((CustomOAuth2User) oAuth2User).getEmail() : oAuth2User.getAttribute("email");

        String token = jwtUtil.generateToken(email); // 이메일로 JWT 토큰 생성

        // 프론트엔드에 JWT 토큰을 전달하기 위해 리다이렉트 URL 에 토큰 추가
        String redirectUrl = UriComponentsBuilder.fromUriString(frontendRedirectUrl)
                .queryParam("token", token)
                .build().toUriString();

        getRedirectStrategy().sendRedirect(request, response, redirectUrl);
    }

}
