package com.gamehubstore.shipping_mscv.client;

import com.gamehubstore.shipping_mscv.models.dtos.OrderDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

// El "name" debe coincidir con el nombre registrado en Eureka/Gateway, la "url" es para tus pruebas locales
@FeignClient(name = "order-mscv", path = "/api/v1/orders")
public interface OrderClient {

    // 1. Obtener los datos de la orden para verificar si está "PAGADA"
    @GetMapping("/{orderId}")
    OrderDTO getOrderById(@PathVariable("orderId") Long orderId);

    // 2. Modificar el estado de la orden de compra a lo largo del ciclo logístico
    // Nota: Si tu order-service usa @PatchMapping, cambia la anotación de abajo por @PatchMapping
    @org.springframework.web.bind.annotation.PatchMapping("/{orderId}/estado")
    void updateEstado(
            @PathVariable("orderId") Long orderId,
            @RequestParam("estado") String estado
    );
}