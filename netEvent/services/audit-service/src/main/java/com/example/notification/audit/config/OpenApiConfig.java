package com.example.notification.audit.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("Audit Service API")
                .version("1.0.0")
                .description("API truy vấn lịch sử Audit Log & Traceability cho tiến trình xử lý thông báo.")
                .contact(new Contact()
                    .name("NetEvent Development Team")
                    .email("support@netevent.example.com")));
    }
}
