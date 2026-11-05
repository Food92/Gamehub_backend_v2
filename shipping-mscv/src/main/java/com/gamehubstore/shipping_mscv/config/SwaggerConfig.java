package com.gamehubstore.shipping_mscv.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    // @Bean: springdoc usa este objeto para construir la página de Swagger UI.
    @Bean
    public OpenAPI customOpenApi() {
        return new OpenAPI()
                // Info: cabecera que se muestra arriba en /swagger-ui/index.html
                .info(new Info()
                        .title("GameHub Store - Shipping Microservice API") // 👈 Título cambiado
                        .version("1.0")
                        .description("API para la gestión logística, control de despachos, asignación de tracking y flujos de estados de entrega para compras de videojuegos y hardware")); // 👈 Descripción adaptada
    }
}
