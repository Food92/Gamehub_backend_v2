package com.gamehubstore.warranty_mscv.client;

import com.gamehubstore.warranty_mscv.models.dtos.ProductDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "product-mscv")
public interface ProductClient { // 👈 2. OBLIGATORIO: Agregar "public"
    @GetMapping("/api/v1/products/{id}")
    ProductDTO getProductById(@PathVariable("id") Long id);
}