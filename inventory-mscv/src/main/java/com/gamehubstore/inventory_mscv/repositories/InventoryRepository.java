package com.gamehubstore.inventory_mscv.repositories;

import com.gamehubstore.inventory_mscv.models.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import java.util.Optional;

import java.util.List;

public interface InventoryRepository extends JpaRepository<Inventory, Long> {
    List<Inventory> findByIdProducto(Long idProducto);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select i from Inventory i where i.idProducto = :productId")
    Optional<Inventory> findByIdProductoForUpdate(@Param("productId") Long productId);
    List<Inventory> findByUbicacion(String ubicacion);
}
