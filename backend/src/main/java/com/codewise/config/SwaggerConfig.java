package com.codewise.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig { // Swagger 설정을 통해 API 문서화 지원

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI().info(
                new Info()
                        .title("CodeWise API") // API 문서의 제목 설정
                        .version("1.0")
                        .description("AI 기반 실시간 코드 분석 시스템 API 문서")
        );
    }
}
