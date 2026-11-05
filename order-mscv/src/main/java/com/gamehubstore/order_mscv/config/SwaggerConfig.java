package com.gamehubstore.order_mscv.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    // @Bean: springdoc usa este objeto para construir la pagina de Swagger UI.
    @Bean
    public OpenAPI customOpenApi(){
        return new OpenAPI()
                // Info: cabecera que se muestra arriba en /docs/swagger-ui.html.
                .info(new Info()
                        .title("GameHub Store - Order Microservice API")
                        .version("1.0")
                        .description("API maestra para la orquestación del ciclo de vida de órdenes de compra, cálculo de totales y coordinación distribuida de stock."));
    }
}