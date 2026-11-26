package com.gamehubstore.payment_mscv.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gamehubstore.payment_mscv.client.OrderClient;
import com.gamehubstore.payment_mscv.exceptions.PaymentException;
import com.gamehubstore.payment_mscv.models.Payment;
import com.gamehubstore.payment_mscv.models.dtos.OrderClientDTO; // 👈 Cambiado aquí
import com.gamehubstore.payment_mscv.models.dtos.PaymentDTO;
import com.gamehubstore.payment_mscv.models.dtos.SimularPagoRequest;
import com.gamehubstore.payment_mscv.repositories.PaymentRepository;
import com.gamehubstore.payment_mscv.services.PaymentService;
import feign.FeignException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
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

        // 2. Validar estado y monto de la orden
        if ("PAGADA".equalsIgnoreCase(order.getEstado())) {
            throw new PaymentException("La orden #" + paymentDTO.getOrderId() + " ya se encuentra PAGADA");
        }
        if ("CANCELADA".equalsIgnoreCase(order.getEstado()) || "RECHAZADA".equalsIgnoreCase(order.getEstado())) {
            throw new PaymentException("La orden #" + paymentDTO.getOrderId() + " está " + order.getEstado() + " y no admite pagos");
        }
        if (order.getTotal() == null || paymentDTO.getMonto() == null
                || Math.abs(order.getTotal() - paymentDTO.getMonto()) > 0.01) {
            throw new PaymentException("El monto no coincide con el total de la orden (" + order.getTotal() + ")");
        }

        // 3. Evitar duplicados (Corregido a PaymentException)
        boolean yaExistePagoAprobado = paymentRepository.findByOrderId(paymentDTO.getOrderId())
                .stream()
                .anyMatch(p -> "APROBADO".equalsIgnoreCase(p.getEstadoPago()));

        if (yaExistePagoAprobado) {
            throw new PaymentException("Ya existe un pago aprobado para esta orden");
        }

        String estadoNormalizado = (paymentDTO.getEstadoPago() == null || paymentDTO.getEstadoPago().isBlank())
                ? "APROBADO"
                : paymentDTO.getEstadoPago().trim().toUpperCase();
        String tipoNormalizado = (paymentDTO.getTipoPago() == null || paymentDTO.getTipoPago().isBlank())
                ? "TARJETA_CREDITO"
                : paymentDTO.getTipoPago().trim().toUpperCase();

        // 4. Crear pago (Entidad)
        Payment payment = new Payment();
        payment.setOrderId(paymentDTO.getOrderId());
        payment.setMonto(order.getTotal());
        payment.setTipoPago(tipoNormalizado);
        payment.setEstadoPago(estadoNormalizado);
        payment.setCodigoPago("PAY-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
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
    @Transactional
    public PaymentDTO simularPago(SimularPagoRequest request) {
        if (request == null || request.getOrderId() == null) {
            throw new PaymentException("El ID de la orden (orderId) es obligatorio");
        }
        OrderClientDTO order;
        try {
            order = orderClient.getOrderById(request.getOrderId());
            if (order == null) {
                throw new PaymentException("Order no existe");
            }
        } catch (FeignException.NotFound e) {
            throw new PaymentException("Order no existe");
        }

        PaymentDTO dto = new PaymentDTO();
        dto.setOrderId(request.getOrderId());
        dto.setMonto(request.getMonto() != null ? request.getMonto() : order.getTotal());
        dto.setTipoPago(request.getTipoPago() != null && !request.getTipoPago().isBlank()
                ? request.getTipoPago().trim().toUpperCase()
                : "TARJETA_CREDITO");
        dto.setEstadoPago(request.isSimularRechazo() ? "RECHAZADO" : "APROBADO");
        return save(dto);
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

    @Override
    @Transactional
    public Payment confirmarPorOrden(Long orderId) {
        Payment ultimo = obtenerUltimoPagoDeOrden(orderId);
        return updatePayment("APROBADO", ultimo.getPaymentId());
    }

    @Override
    @Transactional
    public Payment anularPorOrden(Long orderId) {
        Payment ultimo = obtenerUltimoPagoDeOrden(orderId);
        return updatePayment("RECHAZADO", ultimo.getPaymentId());
    }

    @Override
    @Transactional
    public Payment revertirPorOrden(Long orderId) {
        Payment ultimo = obtenerUltimoPagoDeOrden(orderId);
        return updatePayment("REEMBOLSADO", ultimo.getPaymentId());
    }

    private Payment obtenerUltimoPagoDeOrden(Long orderId) {
        return paymentRepository.findByOrderId(orderId).stream()
                .max(Comparator.comparing(Payment::getPaymentId))
                .orElseThrow(() -> new PaymentException("No existen pagos asociados a la orden: " + orderId));
    }
}