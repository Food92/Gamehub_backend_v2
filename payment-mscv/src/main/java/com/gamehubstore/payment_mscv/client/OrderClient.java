package com.gamehubstore.payment_mscv.client;

import com.gamehubstore.payment_mscv.models.dtos.OrderClientDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestParam;

// "order-mscv" debe coincidir con el 'spring.application.name' del microservicio de órdenes.
// Si no usas un servidor de descubrimiento como Eureka, debes especificar la propiedad 'url'.
@FeignClient(name = "order-mscv", path = "/api/v1/orders")
public interface OrderClient {

    /**
     * Se comunica con el GET del OrderController para obtener los datos de la orden en formato DTO.
     * Útil para validar que la orden exista y que el monto coincida antes de procesar el pago.
     */
    @GetMapping("/{orderId}/dto")
    OrderClientDTO getOrderById(@PathVariable("orderId") Long orderId);

    /**
     * Se comunica con el PatchMapping del OrderController del vendedor/sistema.
     * Cambia el estado de la orden (por ejemplo, a 'PAGADA' o 'RECHAZADA') según el resultado de la transacción.
     */
    @PatchMapping("/{orderId}/estado")
    OrderClientDTO updateEstado(
            @PathVariable("orderId") Long orderId,
            @RequestParam("estado") String estado
    );
}