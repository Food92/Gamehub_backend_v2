package com.gamehubstore.payment_mscv.repositories;

import com.gamehubstore.payment_mscv.models.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    // Una orden puede tener múltiples intentos de pago (ej: uno rechazado y luego uno aprobado)
    List<Payment> findByOrderId(Long orderId);

    // CORRECCIÓN CRÍTICA: Al ser 'unique = true', debe retornar un Optional en lugar de una List
    Optional<Payment> findByCodigoPago(String codigoPago);

    // Mantiene el sentido: Retorna todos los pagos con un estado específico (ej: APROBADO)
    List<Payment> findByEstadoPago(String estadoPago);

    // Mantiene el sentido: Retorna todos los pagos de un tipo (ej: TARJETA_CREDITO)
    List<Payment> findByTipoPago(String tipoPago);
}