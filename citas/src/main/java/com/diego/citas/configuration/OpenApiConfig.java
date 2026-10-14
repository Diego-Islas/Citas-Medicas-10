package com.diego.citas.configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Value("${base.url}")
    private String baseUrl;

    @Value("${gateway.url}")
    private String gatewayUrl;

    @Bean
    public OpenAPI openApi() {
        return new OpenAPI()
                .info(
                        new Info()
                                .title("API de Citas")
                                .version("1.0.0")
                                .description("API para la gestión de citas médicas")
                );
    }
}