package com.gamehubstore.user_mscv.services;

import com.gamehubstore.user_mscv.models.dtos.DireccionDTO;

import java.util.List;

public interface DireccionService {
    DireccionDTO save(DireccionDTO direccionDTO);
    List<DireccionDTO> findByUserId(Long id);
    void delete(Long id);
}