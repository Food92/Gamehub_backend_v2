package com.gamehubstore.shipping_mscv.repositories;

import com.gamehubstore.shipping_mscv.models.Shipping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RepositoryShipping extends JpaRepository<Shipping, Long> {

    // 1. Listar despachos por el ID de la orden (Usa orderId de tu entidad)
    List<Shipping> findByOrderId(Long orderId);

    // 2. Listar despachos por su estado actual (CREADO, EN_TRANSITO, etc.)
    List<Shipping> findByEstado(String estado);

    // 3. Buscar por número de guía único (Devuelve un Optional para validar si ya existe)
    Optional<Shipping> findByTracking(String tracking);

    List<Shipping> findByUserId(Long userId);
}
