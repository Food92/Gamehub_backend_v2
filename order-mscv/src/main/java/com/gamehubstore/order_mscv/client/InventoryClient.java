package com.gamehubstore.order_mscv.client;
import org.springframework.cloud.openfeign.FeignClient; import org.springframework.web.bind.annotation.*;
@FeignClient(name="inventory-mscv",path="/api/v1/inventories")
public interface InventoryClient {
 @PutMapping("/reservations/{orderId}/{idProducto}") Object reservar(@PathVariable Long orderId,@PathVariable Long idProducto,@RequestParam Long cantidad);
 @PutMapping("/reservations/{orderId}/{idProducto}/confirm") Object confirmar(@PathVariable Long orderId,@PathVariable Long idProducto);
 @DeleteMapping("/reservations/{orderId}/{idProducto}") Object liberar(@PathVariable Long orderId,@PathVariable Long idProducto);
}
