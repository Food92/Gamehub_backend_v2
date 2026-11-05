package com.gamehubstore.warranty_mscv.repositories;

import com.gamehubstore.warranty_mscv.models.Warranty;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WarrantyRepository  extends JpaRepository<Warranty, Long> {
    List<Warranty> findByUserId(Long userId);
    List<Warranty> findByProductId(Long productId);
    List<Warranty> findByEstado(String estado);
}
