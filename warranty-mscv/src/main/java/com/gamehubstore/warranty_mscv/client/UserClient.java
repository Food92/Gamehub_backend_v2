package com.gamehubstore.warranty_mscv.client;


import com.gamehubstore.warranty_mscv.models.dtos.UserDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "user-mscv")
public interface UserClient { // 👈 2. OBLIGATORIO: Agregar "public"
    @GetMapping("/api/v1/users/{id}")
    UserDTO getUserById(@PathVariable("id") Long id);
}



