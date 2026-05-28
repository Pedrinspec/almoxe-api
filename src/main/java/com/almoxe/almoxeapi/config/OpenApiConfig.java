package com.almoxe.almoxeapi.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI almoxeOpenAPI() {
        return new OpenAPI().info(new Info()
                .title("Almoxe API")
                .description("API de controle de almoxarifado e movimentação de materiais.")
                .version("0.0.1-SNAPSHOT"));
    }
}
