package com.codewise.config;

import com.codewise.util.JwtUtil;
import org.springframework.context.annotation.*;
import org.springframework.security.authentication.*;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import com.codewise.service.CustomOAuth2UserService; // 새로 추가한 서비스 임포트

@Configuration // 스프링 설정 클래스임을 명시
public class SecurityConfig { // 스프링 시큐리티 설정을 통해 인증 및 권한 제어 구성

    private final JwtUtil jwtUtil;
    private final CustomOAuth2UserService customOAuth2UserService; // 새로 추가할 OAuth2 서비스 주입
    private final OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler; // 새로 추가할 성공 핸들러 주입

    // 생성자에 추가
    public SecurityConfig(JwtUtil jwtUtil, CustomOAuth2UserService customOAuth2UserService, OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler) {
        this.jwtUtil = jwtUtil;
        this.customOAuth2UserService = customOAuth2UserService;
        this.oAuth2AuthenticationSuccessHandler = oAuth2AuthenticationSuccessHandler;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable) // 람다식으로 변경
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                                .requestMatchers("/", "/auth/**", "/swagger-ui/**", "/v3/api-docs/**", "/ws/**", "/oauth2/**", "/login/oauth2/code/**").permitAll() // OAuth2 관련 경로 허용
                                // 특정 경로(루트, 인증 관련, Swagger UI, Swagger API docs, WebSocket)는 인증 없이 접근 허용
                                .anyRequest().authenticated()
                        // 그 외 모든 요청은 인증된 사용자만 접근 허용
                )
                .oauth2Login(oauth2 -> oauth2 // OAuth2 로그인 설정 추가
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(customOAuth2UserService) // 커스텀 User 서비스 등록
                        )
                        .successHandler(oAuth2AuthenticationSuccessHandler) // 로그인 성공 시 JWT 토큰 발행을 위한 핸들러
                )
                .addFilterBefore(new JwtAuthenticationFilter(jwtUtil),
                        UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public AuthenticationManager authenticationManager() {
        return new ProviderManager(new DaoAuthenticationProvider());
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}