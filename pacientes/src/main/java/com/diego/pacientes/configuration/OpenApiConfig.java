package com.diego.pacientes.configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Value("${base.url}")
    private String baseUrl;

    @Value("${gateway.url}")
    private String gatewayUrl;

    @Bean
    public OpenAPI openApi(){
        return new OpenAPI().info(
                new Info()
                        .title("API de pacientes")
                        .version("1.0.0")
                        .description("API para la gestion de pacientes medicos")
                )
                .servers(List.of(
                        new Server()
                                .url(baseUrl)
                                .description("Servidor local"),

                        new Server()
                                .url(gatewayUrl)
                                .description("API Gateway")
                ));
    }
}
