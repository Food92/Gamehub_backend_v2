package com.gamehubstore.payment_mscv.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerPayment {
    @Bean
    public OpenAPI customOpenApi() {
        return new OpenAPI()
                // Info: cabecera que se muestra arriba en /swagger-ui/index.html
                .info(new Info()
                        .title("GameHub Store - Payment Microservice API") // 👈 Título adaptado
                        .version("1.0")
                        .description("API para el procesamiento seguro de transacciones, validación de montos con órdenes y control de pasarelas de pago financieros")); // 👈 Descripción adaptada
    }
}
