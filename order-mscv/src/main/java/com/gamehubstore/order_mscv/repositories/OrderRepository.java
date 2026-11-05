package com.gamehubstore.order_mscv.repositories;

import com.gamehubstore.order_mscv.models.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.time.LocalDateTime;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    /**
     * Busca todas las órdenes que pertenecen a un usuario específico.
     * Útil para mostrar el historial de compras en el perfil del cliente.
     */
    List<Order> findByUserId(Long userId);

    /**
     * Filtra las órdenes por su estado actual (ej: "CREADA", "PAGADA", "CANCELADA").
     * Útil para paneles de administración y auditorías del sistema.
     */
    List<Order> findByEstado(String estado);

    List<Order> findByEstadoAndSagaUpdatedAtBefore(String estado, LocalDateTime before);
}