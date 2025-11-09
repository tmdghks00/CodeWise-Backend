package com.codewise.config;

import org.springframework.context.annotation.*;
import org.springframework.web.cors.*;
import org.springframework.web.filter.CorsFilter;

@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();

        // ✅ 프론트 주소 허용 (Vercel 도메인)
        config.addAllowedOrigin("https://codewise-frontend.vercel.app");

        // ✅ Backend HTTPS 주소도 WebSocket 허용
        config.addAllowedOrigin("https://codewise-backend.duckdns.org");

        config.addAllowedHeader("*");
        config.addAllowedMethod("*");
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
}
