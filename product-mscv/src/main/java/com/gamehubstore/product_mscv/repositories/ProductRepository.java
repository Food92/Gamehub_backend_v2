package com.gamehubstore.product_mscv.repositories;

import com.gamehubstore.product_mscv.models.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByMarca(String marca);
    List<Product> findByIdCategory(Long idCategory);
    List<Product> findByEstado(Boolean estado);
    boolean existsByNombreProduct(String nombreProduct);

}
