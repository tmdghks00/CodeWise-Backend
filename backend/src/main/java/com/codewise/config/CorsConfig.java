package com.codewise.config;

import org.springframework.context.annotation.*;
import org.springframework.web.cors.*;
import org.springframework.web.filter.CorsFilter;

@Configuration // 스프링 설정 클래스임을 명시
public class CorsConfig { // CORS 설정을 통해 프론트엔드에서의 요청을 허용함
    @Bean // 이 메서드가 반환하는 객체를 스프링 빈으로 등록
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        config.addAllowedOriginPattern("*");
        config.addAllowedHeader("*"); // 모든 HTTP 헤더를 허용
        config.addAllowedMethod("*"); // 모든 HTTP 메서드(GET, POST, PUT, DELETE 등)를 허용
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
}
