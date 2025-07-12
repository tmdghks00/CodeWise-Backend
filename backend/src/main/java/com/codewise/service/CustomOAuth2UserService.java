package com.codewise.service;

import com.codewise.domain.User;
import com.codewise.domain.UserRole;
import com.codewise.repository.UserRepository;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import java.util.Map;
import java.util.Optional;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    public CustomOAuth2UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        String provider = userRequest.getClientRegistration().getRegistrationId(); // "google"
        String providerId = oAuth2User.getName(); // Google sub (사용자 고유 ID)
        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name"); // 사용자 이름 (Google 에서 제공하는 경우)

        Optional<User> optionalUser = userRepository.findByProviderAndProviderId(provider, providerId);

        User user;
        if (optionalUser.isEmpty()) {
            // 새로 가입하는 사용자
            user = User.builder()
                    .email(email)
                    .password(null) // 소셜 로그인 사용자는 비밀번호가 없음
                    .role(UserRole.USER)
                    .provider(provider)
                    .providerId(providerId)
                    .build();
            userRepository.save(user);
        } else {
            // 이미 가입된 사용자
            user = optionalUser.get();
            // 필요하다면 사용자 정보 업데이트 (예: 이메일 변경 등)
            if (user.getEmail() == null || !user.getEmail().equals(email)) {
                user.setEmail(email);
                userRepository.save(user);
            }
        }

        // Spring Security 에서 사용하기 위한 OAuth2User 반환
        // JWT 발행에 필요한 username 으로 email 을 사용하도록 CustomOAuth2User 를 만듭니다.
        return new CustomOAuth2User(user.getEmail(), oAuth2User.getAuthorities(), oAuth2User.getAttributes());
    }
}