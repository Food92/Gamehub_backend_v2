package com.gamehubstore.category_mscv.client;
import org.springframework.cloud.openfeign.FeignClient; import org.springframework.web.bind.annotation.*; import java.util.List; import java.util.Map;
@FeignClient(name="product-mscv") public interface ProductClient { @GetMapping("/api/v1/products/category/{categoryId}") List<Map<String,Object>> byCategory(@PathVariable("categoryId") Long categoryId); }
