package com.gamehubstore.warranty_mscv.services;

import com.gamehubstore.warranty_mscv.models.Warranty;
import com.gamehubstore.warranty_mscv.models.dtos.WarrantyDTO;
import java.util.List;

public interface WarrantyService {
    WarrantyDTO createWarranty(WarrantyDTO warrantyDTO);
    List<Warranty> findByCliente(Long usuarioId);
    List<Warranty> findByProducto(Long productoId);
    List<Warranty> findByEstado(String estado);
    Warranty findById(Long id);
    Warranty updateStatusOrDiagnostic(Long id, String estado, String diagnostico, String resolucion);
    Warranty closeWarranty(Long id, String resolucion);
    List<Warranty> findAll();
}