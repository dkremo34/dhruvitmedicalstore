package com.medicalstore.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI medicalStoreOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Medical Store Management API")
                        .description("API for managing medical store operations")
                        .version("1.0.0"));
    }
}