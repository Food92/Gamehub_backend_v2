package com.gamehubstore.review_mscv.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("GameHub Store - Review Microservice API")
                        .version("1.0")
                        .description("API para la gestión de reseñas, calificaciones y moderación de opiniones de productos."));
    }
}
