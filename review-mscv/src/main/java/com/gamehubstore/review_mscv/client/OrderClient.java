package com.gamehubstore.review_mscv.client; import org.springframework.cloud.openfeign.FeignClient; import org.springframework.web.bind.annotation.*; import java.util.Map;
@FeignClient(name="order-mscv",url="${clients.order.url:http://localhost:8006}") public interface OrderClient { @GetMapping("/api/v1/orders/{id}") Map<String,Object> findOrder(@PathVariable("id") Long id); }
