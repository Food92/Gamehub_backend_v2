package com.gamehubstore.promotion_mscv.repositories;
import com.gamehubstore.promotion_mscv.models.PromotionReservation; import jakarta.persistence.LockModeType; import org.springframework.data.jpa.repository.*; import org.springframework.data.repository.query.Param; import java.util.*;
public interface PromotionReservationRepository extends JpaRepository<PromotionReservation,Long> {
 Optional<PromotionReservation> findByOrderId(Long orderId);
 @Lock(LockModeType.PESSIMISTIC_WRITE) @Query("select r from PromotionReservation r where r.orderId=:orderId") Optional<PromotionReservation> findByOrderIdForUpdate(@Param("orderId") Long orderId);
 long countByCodeIgnoreCaseAndStatus(String code,String status);
}
