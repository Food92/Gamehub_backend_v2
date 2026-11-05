package com.gamehubstore.user_mscv.config;

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
                // Info: cabecera que se muestra arriba en /docs/swagger-ui.html o /swagger-ui/index.html.
                .info(new Info()
                        .title("GameHub Store - User Microservice API")
                        .version("1.0")
                        .description("API para la administración de perfiles de clientes, operadores y administradores, incluyendo datos de contacto y gestión de direcciones de despacho"));
    }
}