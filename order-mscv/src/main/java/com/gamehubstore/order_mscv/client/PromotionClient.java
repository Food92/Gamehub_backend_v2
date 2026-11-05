package com.gamehubstore.order_mscv.client;
import org.springframework.cloud.openfeign.FeignClient; import org.springframework.web.bind.annotation.*; import java.math.BigDecimal; import java.util.Map;
@FeignClient(name="promotion-mscv",path="/api/v1/promotions") public interface PromotionClient {
 @PostMapping("/validate") Map<String,BigDecimal> validate(@RequestParam("code") String code,@RequestParam("total") BigDecimal total);
 @PutMapping("/reservations/{orderId}") Object reserve(@PathVariable("orderId") Long orderId,@RequestParam("code") String code,@RequestParam("total") BigDecimal total);
 @PutMapping("/reservations/{orderId}/confirm") Object confirm(@PathVariable("orderId") Long orderId);
 @DeleteMapping("/reservations/{orderId}") void release(@PathVariable("orderId") Long orderId);
}
