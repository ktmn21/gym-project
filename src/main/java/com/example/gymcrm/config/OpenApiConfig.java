package com.example.gymcrm.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI gymCrmOpenAPI() {
        return new OpenAPI().info(new Info()
                .title("Gym CRM API")
                .description("Gym CRM REST API documentation")
                .version("1.0"));
    }
}