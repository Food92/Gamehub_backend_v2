package com.gamehubstore.inventory_mscv.client;

import com.gamehubstore.inventory_mscv.models.dtos.ProductDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "product-mscv", path = "/api/v1/products")
public interface ProductClient {
    @GetMapping("/{id}")
    ProductDTO findById(@PathVariable("id") Long id);
}