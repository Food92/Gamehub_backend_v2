package com.gamehubstore.promotion_mscv.repositories;
import com.gamehubstore.promotion_mscv.models.Promotion; import jakarta.persistence.LockModeType; import org.springframework.data.jpa.repository.*; import org.springframework.data.repository.query.Param; import java.util.*;
public interface PromotionRepository extends JpaRepository<Promotion,Long> {
 boolean existsByCodeIgnoreCase(String code); Optional<Promotion> findByCodeIgnoreCase(String code); List<Promotion> findByActiveTrue();
 @Lock(LockModeType.PESSIMISTIC_WRITE) @Query("select p from Promotion p where lower(p.code)=lower(:code)") Optional<Promotion> findByCodeForUpdate(@Param("code") String code);
}
