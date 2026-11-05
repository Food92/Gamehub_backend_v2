package com.gamehubstore.product_mscv.services;

import com.gamehubstore.product_mscv.models.Product;
import com.gamehubstore.product_mscv.models.dtos.ProductDTO;

import java.util.List;

public interface ProductService {
    List<ProductDTO> findAll();
    Product findById(Long id);
    Product save(Product product);
    Product update(Product product, Long id);
    void delete(Long id);
    void desactivar(Long id);

    List<Product> findByMarca(String marca);
    List<Product> findByIdCategory(Long idCategory);
    List<Product> findByEstado(Boolean estado);

}
