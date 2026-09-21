package com.example.notification.routing.config;

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
                .title("Routing & Approval Engine Service API")
                .version("1.0.0")
                .description("API xử lý định tuyến thông báo, kiểm tra ma trận phê duyệt và render template.")
                .contact(new Contact()
                    .name("NetEvent Development Team")
                    .email("support@netevent.example.com")));
    }
}
