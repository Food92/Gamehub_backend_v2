package com.gamehubstore.authentificator_mscv.repositories;

import com.gamehubstore.authentificator_mscv.models.Rol;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RolRepository  extends JpaRepository<Rol, Long> {
    Optional<Rol> findByNombre(String nombre);
}

