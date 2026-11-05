package com.gamehubstore.product_mscv.client;

import com.gamehubstore.product_mscv.models.dtos.CategoryDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "category-mscv", path="/api/v1/categories")
public interface CategoryClient {
    @GetMapping("/{id}")
    CategoryDTO findById(@PathVariable Long id);
}
