package com.gamehubstore.warranty_mscv.client;

import com.gamehubstore.warranty_mscv.models.dtos.OrderDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "order-mscv")
public interface OrderClient { // 👈 2. OBLIGATORIO: Agregar "public"
    @GetMapping("/api/v1/orders/{id}")
    OrderDTO getOrderById(@PathVariable("id") Long id);
}