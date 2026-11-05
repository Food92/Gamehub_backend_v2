package com.gamehubstore.payment_mscv.services.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gamehubstore.payment_mscv.client.OrderClient;
import com.gamehubstore.payment_mscv.exceptions.PaymentException;
import com.gamehubstore.payment_mscv.models.Payment;
import com.gamehubstore.payment_mscv.models.dtos.OrderClientDTO; // 👈 Cambiado aquí
import com.gamehubstore.payment_mscv.models.dtos.PaymentDTO;
import com.gamehubstore.payment_mscv.repositories.PaymentRepository;
import com.gamehubstore.payment_mscv.services.PaymentService;
import feign.FeignException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Service
public class PaymentServiceImpl implements PaymentService {
    private static final Logger log = LoggerFactory.getLogger(PaymentServiceImpl.class);

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private OrderClient orderClient;

    @Override
    @Transactional
    public PaymentDTO save(PaymentDTO paymentDTO) {
        log.info("operation={} started", "save");
        OrderClientDTO order; // 👈 Cambiado aquí

        // 1. Validar orden remota con manejo seguro de Feign
        try {
            order = orderClient.getOrderById(paymentDTO.getOrderId());
            if (order == null) {
                throw new PaymentException("Order no existe");
            }
        } catch (FeignException.NotFound e) {
            throw new PaymentException("Order no existe");
        }

        // 2. Validar monto
        if (!order.getTotal().equals(paymentDTO.getMonto())) {
            throw new PaymentException("El monto no coincide con el total de la orden");
        }

        // 3. Evitar duplicados (Corregido a PaymentException)
        boolean yaExistePagoAprobado = paymentRepository.findByOrderId(paymentDTO.getOrderId())
                .stream()
                .anyMatch(p -> "APROBADO".equals(p.getEstadoPago()));

        if (yaExistePagoAprobado) {
            throw new PaymentException("Ya existe un pago aprobado para esta orden");
        }

        // 4. Crear pago (Entidad)
        Payment payment = new Payment();
        payment.setOrderId(paymentDTO.getOrderId());
        payment.setMonto(paymentDTO.getMonto());
        payment.setTipoPago(paymentDTO.getTipoPago());
        payment.setEstadoPago(paymentDTO.getEstadoPago());
        payment.setCodigoPago(UUID.randomUUID().toString());
        payment.setFechaPago(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS));

        Payment guardado = paymentRepository.save(payment);

        // 5. Notificar al order-service según corresponda
        if ("APROBADO".equals(guardado.getEstadoPago())) {
            orderClient.updateEstado(guardado.getOrderId(), "PAGADA");
        } else if ("RECHAZADO".equals(guardado.getEstadoPago())) {
            orderClient.updateEstado(guardado.getOrderId(), "RECHAZADA");
        }

        // 6. Retornar DTO de salida mapeado limpiamente
        PaymentDTO resultadoDTO = new PaymentDTO();
        resultadoDTO.setOrderId(guardado.getOrderId());
        resultadoDTO.setMonto(guardado.getMonto());
        resultadoDTO.setTipoPago(guardado.getTipoPago());
        resultadoDTO.setEstadoPago(guardado.getEstadoPago());
        resultadoDTO.setCodigoPago(guardado.getCodigoPago());
        resultadoDTO.setFechaPago(guardado.getFechaPago());

        return resultadoDTO;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Payment> findByOrder(Long orderId) {
        return paymentRepository.findByOrderId(orderId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Payment> findByEstadoPago(String estadoPago) {
        return paymentRepository.findByEstadoPago(estadoPago);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Payment> findByTipoPago(String tipoPago) {
        return paymentRepository.findByTipoPago(tipoPago);
    }

    @Override
    @Transactional(readOnly = true)
    public Payment findById(Long id) {
        return paymentRepository.findById(id)
                .orElseThrow(() -> new PaymentException("Payment no encontrado"));
    }

    @Override
    @Transactional
    public Payment updatePayment(String estadoPago, Long paymentId) {
        Payment pago = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentException("Pago no encontrado"));

        pago.setEstadoPago(estadoPago);
        Payment actualizado = paymentRepository.save(pago);

        // Notificar al order-service el cambio de estado
        if ("APROBADO".equals(estadoPago)) {
            orderClient.updateEstado(pago.getOrderId(), "PAGADA");
        } else if ("RECHAZADO".equals(estadoPago)) {
            orderClient.updateEstado(pago.getOrderId(), "RECHAZADA");
        }

        return actualizado;
    }

    @Override
    @Transactional
    public Payment cancelPayment(Long paymentId) {
        Payment existing = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentException("Payment no encontrado"));

        existing.setEstadoPago("CANCELADO");
        Payment pagoCancelado = paymentRepository.save(existing);

        // Notificar al order-service
        orderClient.updateEstado(pagoCancelado.getOrderId(), "CANCELADA");

        return pagoCancelado;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Payment> findAll() {
        return paymentRepository.findAll();
    }
}