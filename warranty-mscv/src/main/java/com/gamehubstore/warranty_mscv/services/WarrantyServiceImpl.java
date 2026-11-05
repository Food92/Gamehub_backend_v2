package com.gamehubstore.warranty_mscv.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import com.gamehubstore.warranty_mscv.client.OrderClient;
import com.gamehubstore.warranty_mscv.client.ProductClient;
import com.gamehubstore.warranty_mscv.client.UserClient;
import com.gamehubstore.warranty_mscv.exceptions.WarrantyException;
import com.gamehubstore.warranty_mscv.models.Warranty;
import com.gamehubstore.warranty_mscv.models.dtos.OrderDTO;
import com.gamehubstore.warranty_mscv.models.dtos.ProductDTO;
import com.gamehubstore.warranty_mscv.models.dtos.UserDTO;
import com.gamehubstore.warranty_mscv.models.dtos.WarrantyDTO;
import com.gamehubstore.warranty_mscv.repositories.WarrantyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class WarrantyServiceImpl implements WarrantyService {
    private static final Logger log = LoggerFactory.getLogger(WarrantyServiceImpl.class);

    @Autowired
    private WarrantyRepository warrantyRepository;

    @Autowired
    private UserClient userClient;

    @Autowired
    private OrderClient orderClient;

    @Autowired
    private ProductClient productClient;

    /**
     * Crear solicitud de garantía.
     * REGLA: No aceptar garantía sin compra asociada y que esté dentro del plazo.
     */
    @Override
    @Transactional
    public WarrantyDTO createWarranty(WarrantyDTO dto) {
        log.info("operation=createWarranty started");
        // 1. Validar que existan los registros en los microservicios externos por Feign
        UserDTO user = userClient.getUserById(dto.getUsuarioId());
        OrderDTO order = orderClient.getOrderById(dto.getOrdenId());
        ProductDTO product = productClient.getProductById(dto.getProductoId());

        // REGLA MINIMA 1: No aceptar garantía sin compra asociada (Estado correcto de la orden)
        if (!"ENTREGADA".equalsIgnoreCase(order.getEstado()) && !"PAGADA".equalsIgnoreCase(order.getEstado())) {
            throw new WarrantyException("La orden asociada debe estar PAGADA o ENTREGADA para aplicar garantía.");
        }
        if (!dto.getUsuarioId().equals(order.getUsuarioId())) {
            throw new WarrantyException("La orden no pertenece al usuario solicitante.");
        }
        boolean productoComprado = order.getDetails() != null && order.getDetails().stream()
                .anyMatch(detail -> dto.getProductoId().equals(detail.getProductoId()));
        if (!productoComprado) throw new WarrantyException("El producto no pertenece a la orden asociada.");

        // REGLA MÍNIMA 2: No aceptar garantía fuera de plazo
        LocalDateTime fechaLimite = order.getFechaCompra().plusMonths(product.getMesesGarantia());
        if (LocalDateTime.now().isAfter(fechaLimite)) {
            throw new WarrantyException("Plazo de garantía vencido. Cobertura límite de este producto: "
                    + product.getMesesGarantia() + " meses.");
        }

        Warranty warranty = new Warranty();
        warranty.setUserId(dto.getUsuarioId());
        warranty.setOrdenId(dto.getOrdenId());
        warranty.setProductId(dto.getProductoId());
        warranty.setMotivo(dto.getMotivo());
        warranty.setEstado("CREADO");
        warranty.setFechaSolicitud(LocalDateTime.now());

        Warranty saved = warrantyRepository.save(warranty);

        // Mapear los datos de vuelta al DTO de respuesta
        dto.setId(saved.getId());
        dto.setEstado(saved.getEstado());
        return dto;
    }

    /**
     * Buscar garantía por ID.
     */
    @Override
    @Transactional(readOnly = true)
    public Warranty findById(Long id) {
        return warrantyRepository.findById(id)
                .orElseThrow(() -> new WarrantyException("Garantía no encontrada con el ID: " + id));
    }

    /**
     * Actualizar estado, diagnóstico o resolución.
     */
    @Override
    @Transactional
    public Warranty updateStatusOrDiagnostic(Long id, String estado, String diagnostico, String resolucion) {
        Warranty existente = findById(id);

        if ("CERRADA".equalsIgnoreCase(existente.getEstado()) || "CERRADO".equalsIgnoreCase(existente.getEstado())) {
            throw new WarrantyException("No se puede modificar una solicitud de garantía que ya está CERRADA.");
        }

        if (estado != null && !estado.isBlank()) {
            existente.setEstado(estado.toUpperCase().trim());
        }
        if (diagnostico != null) {
            existente.setDiagnostico(diagnostico);
        }
        if (resolucion != null) {
            existente.setResolucion(resolucion);
        }

        return warrantyRepository.save(existente);
    }

    /**
     * Cerrar solicitud de garantía.
     * REGLA: No cerrar garantía sin resolución clara.
     */
    @Override
    @Transactional
    public Warranty closeWarranty(Long id, String resolucion) {
        Warranty warranty = findById(id);

        // REGLA MÍNIMA 3: No cerrar garantía sin resolución
        if (resolucion == null || resolucion.isBlank()) {
            throw new WarrantyException("REGLA INCUMPLIDA: No se puede proceder a cerrar el ticket sin especificar una resolución.");
        }

        warranty.setResolucion(resolucion);
        warranty.setEstado("CERRADA");

        return warrantyRepository.save(warranty);
    }

    /**
     * Listar garantías por cliente.
     */
    @Override
    @Transactional(readOnly = true)
    public List<Warranty> findByCliente(Long usuarioId) {
        return warrantyRepository.findByUserId(usuarioId);
    }

    /**
     * Listar garantías por producto.
     */
    @Override
    @Transactional(readOnly = true)
    public List<Warranty> findByProducto(Long productoId) {
        return warrantyRepository.findByProductId(productoId);
    }

    /**
     * Listar garantías por estado.
     */
    @Override
    @Transactional(readOnly = true)
    public List<Warranty> findByEstado(String estado) {
        return warrantyRepository.findByEstado(estado.toUpperCase().trim());
    }

    /**
     * Listar todas las garantías de forma global.
     */
    @Override
    @Transactional(readOnly = true)
    public List<Warranty> findAll() {
        return warrantyRepository.findAll();
    }
}
