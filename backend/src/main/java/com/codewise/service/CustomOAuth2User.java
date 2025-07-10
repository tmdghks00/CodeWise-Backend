package com.codewise.service;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.Map;

public class CustomOAuth2User implements OAuth2User {

    private String email; // 우리의 시스템에서 username 으로 사용될 이메일
    private Collection<? extends GrantedAuthority> authorities;
    private Map<String, Object> attributes; // Google 에서 받은 원본 사용자 속성

    public CustomOAuth2User(String email, Collection<? extends GrantedAuthority> authorities, Map<String, Object> attributes) {
        this.email = email;
        this.authorities = authorities;
        this.attributes = attributes;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getName() {
        // Spring Security 의 기본 OAuth2User 구현은 sub(고유 ID)를 반환하지만,
        // 우리는 JWT 발행 시 email 을 사용하므로 email 을 반환하도록 재정의합니다.
        return this.email;
    }

    public String getEmail() {
        return email;
    }
}