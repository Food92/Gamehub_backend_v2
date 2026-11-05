package com.gamehubstore.inventory_mscv.services;

import com.gamehubstore.inventory_mscv.models.Inventory;
import com.gamehubstore.inventory_mscv.models.InventoryReservation;
import com.gamehubstore.inventory_mscv.models.dtos.InventoryDTO;

import java.util.List;

public interface InventoryService {

    // CRUD Obligatorio del Microservicio [cite: 18, 129]
    InventoryDTO save(InventoryDTO inventoryDTO); // Crear registro de stock para producto [cite: 130]

    Inventory findById(Long id); // Buscar stock por ID [cite: 132]

    Inventory update(Long id, Inventory inventory); // Actualizar cantidades disponibles o reservadas [cite: 133]

    void deleteById(Long id); // Eliminar o cerrar registro de stock obsoleto [cite: 134]

    List<Inventory> findAll(); // Listar todo el stock disponible [cite: 131]

    // Consultas específicas requeridas por el catálogo y el negocio
    List<Inventory> findByIdProducto(Long idProducto); // Listar stock por producto [cite: 131]

    List<Inventory> findByUbicacion(String ubicacion); // Listar stock por bodega/ubicación [cite: 131]

    // Métodos Críticos para los Flujos Integradores del Caso Semestral
    // Regla: Registrar movimientos de inventario y permitir reservas temporales (Flujo 3) [cite: 128, 276]
    Inventory reservarStock(Long idProducto, Long cantidad);

    // Regla: Descontar stock real tras la aprobación del pago (Flujo 6) [cite: 140, 279]
    Inventory salidaStock(Long idProducto, Long cantidad);

    InventoryReservation reservarParaOrden(Long orderId, Long idProducto, Long cantidad);
    InventoryReservation liberarReserva(Long orderId, Long idProducto);
    InventoryReservation confirmarReserva(Long orderId, Long idProducto);
}