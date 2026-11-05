package com.gamehubstore.user_mscv.repositories;

import com.gamehubstore.user_mscv.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserRepository extends JpaRepository<User, Long> {

    List<User> findByEstadoTrue(); // user activo
    List<User> findByEstadoFalse();// user inactivo
    boolean existsByCorreo(String correo);


}
