package com.gamehubstore.user_mscv.services;

import com.gamehubstore.user_mscv.models.User;
import com.gamehubstore.user_mscv.models.dtos.UserDTO;

import java.util.List;

public interface UserService {
    UserDTO save(UserDTO userDTO);
    User findById(Long id);
    User update(Long id, User user);
    void delete(Long id);
    void desactivar(Long id);

    List<User> findAll();
    List<User> findByEstadoTrue();
    List<User> findByEstadoFalse();
}