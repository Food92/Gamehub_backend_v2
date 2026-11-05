package com.gamehubstore.payment_mscv.services;

import com.gamehubstore.payment_mscv.models.Payment;
import com.gamehubstore.payment_mscv.models.dtos.PaymentDTO;

import java.util.List;

public interface PaymentService {
    PaymentDTO save(PaymentDTO paymentDTO); // Retorna PaymentDTO para el Controller
    List<Payment> findByOrder(Long orderId);
    List<Payment> findByEstadoPago(String estadoPago);
    List<Payment> findByTipoPago(String tipoPago);
    Payment findById(Long id);
    Payment updatePayment(String estadoPago, Long paymentId); // Corregido a CamelCase
    Payment cancelPayment(Long paymentId);                    // Corregido a CamelCase
    List<Payment> findAll();
}