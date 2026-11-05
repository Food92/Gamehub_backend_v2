package com.gamehubstore.user_mscv.repositories;

import com.gamehubstore.user_mscv.models.Direccion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DireccionRepository extends JpaRepository<Direccion, Long> {
    List<Direccion> findByUserId(Long id);
}
