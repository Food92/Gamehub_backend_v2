package com.gamehubstore.authentificator_mscv.repositories;

import com.gamehubstore.authentificator_mscv.models.CuentaAcceso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CuentaAccesoRepository extends JpaRepository<CuentaAcceso, Long> {

    // Buscar por email para realizar el inicio de sesión
    Optional<CuentaAcceso> findByEmail(String email);

    // Validar si el correo electrónico ya está tomado en el registro
    boolean existsByEmail(String email);
}