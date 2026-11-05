package com.gamehubstore.order_mscv.client;

import com.gamehubstore.order_mscv.models.dtos.UserClientDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

// "user-mscv" debe ser el nombre del microservicio de usuarios en Eureka/ConfigServer
@FeignClient(name = "user-mscv", path = "/api/v1/users") // 👈 Ajusta el puerto (ej: 8081) si no usas Eureka
public interface UserClient {

    // Conecta con el GET de tu controlador de usuarios que busca por ID
    @GetMapping("/{id}")
    UserClientDTO getUserById(@PathVariable("id") Long id);
}