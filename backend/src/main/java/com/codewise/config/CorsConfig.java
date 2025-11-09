package com.codewise.config;

import org.springframework.context.annotation.*;
import org.springframework.web.cors.*;
import org.springframework.web.filter.CorsFilter;

@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();

        // 프론트(Vercel)
        config.addAllowedOrigin("https://codewise-frontend.vercel.app");

        // 백엔드(EC2, DuckDNS)
        config.addAllowedOrigin("https://codewise-backend.duckdns.org");

        // AI 서버(Render)
        config.addAllowedOrigin("https://codewise-ai-server.onrender.com");

        config.addAllowedHeader("*");
        config.addAllowedMethod("*");
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
}
