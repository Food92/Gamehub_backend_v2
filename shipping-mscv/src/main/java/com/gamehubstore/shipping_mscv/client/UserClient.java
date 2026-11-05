package com.gamehubstore.shipping_mscv.client;

import com.gamehubstore.shipping_mscv.models.dtos.UserDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "user-mscv")
public interface UserClient {

    // Obtener los datos del usuario para verificar y heredar su dirección
    @GetMapping("/api/v1/users/{userId}")
    UserDTO getUserById(@PathVariable("userId") Long userId);
}