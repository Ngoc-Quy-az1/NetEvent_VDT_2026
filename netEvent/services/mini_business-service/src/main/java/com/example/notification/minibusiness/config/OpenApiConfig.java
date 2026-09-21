package com.example.notification.minibusiness.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("NetEvent - Mini Business Service API")
                        .version("1.0.0")
                        .description("RESTful APIs quản lý Quy trình nghiệp vụ Profile, Template thông báo, Business Rules, Accounts và Events.")
                        .contact(new Contact()
                                .name("NetEvent Development Team")
                                .email("dev@netevent.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0")));
    }
}
