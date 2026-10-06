package com.example.notification.ott.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI ottWorkerOpenApi() {
        return new OpenAPI().info(new Info()
                .title("NetEvent OTT Worker API")
                .version("v1")
                .description("API kiểm thử gửi Telegram. Cấu hình TELEGRAM_BOT_TOKEN trước khi gọi."));
    }
}
