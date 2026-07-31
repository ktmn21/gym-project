package com.example.gymcrm.config;

import org.springdoc.core.configuration.SpringDocConfiguration;
import org.springdoc.core.properties.SpringDocConfigProperties;
import org.springdoc.core.properties.SwaggerUiConfigProperties;
import org.springdoc.core.properties.SwaggerUiOAuthProperties;
import org.springdoc.core.providers.ObjectMapperProvider;
import org.springdoc.webmvc.core.configuration.SpringDocWebMvcConfiguration;
import org.springdoc.webmvc.ui.SwaggerConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;

@Configuration
@Import({
        SpringDocConfiguration.class,
        SpringDocConfigProperties.class,
        SwaggerUiConfigProperties.class,
        SwaggerUiOAuthProperties.class,
        SpringDocWebMvcConfiguration.class,
        SwaggerConfig.class
})
public class SpringDocConfig {

    @Bean
    public OpenAPI gymCrmOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Gym CRM API")
                        .description("Gym CRM REST API documentation")
                        .version("1.0"));
    }
}