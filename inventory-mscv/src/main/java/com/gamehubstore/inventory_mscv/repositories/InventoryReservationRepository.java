package com.gamehubstore.inventory_mscv.repositories;
import com.gamehubstore.inventory_mscv.models.InventoryReservation;
import jakarta.persistence.LockModeType; import org.springframework.data.jpa.repository.*; import org.springframework.data.repository.query.Param;
import java.util.*;
public interface InventoryReservationRepository extends JpaRepository<InventoryReservation,Long> {
 Optional<InventoryReservation> findByOrderIdAndProductId(Long orderId, Long productId);
 @Lock(LockModeType.PESSIMISTIC_WRITE) @Query("select r from InventoryReservation r where r.orderId=:orderId and r.productId=:productId")
 Optional<InventoryReservation> findByOrderIdAndProductIdForUpdate(@Param("orderId") Long orderId,@Param("productId") Long productId);
 List<InventoryReservation> findAllByOrderId(Long orderId);
}
