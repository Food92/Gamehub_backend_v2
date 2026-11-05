package com.gamehubstore.payment_mscv.models;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal; // Opcional si decides migrar a BigDecimal
import java.time.LocalDateTime;

@Setter
@Getter
@ToString
@NoArgsConstructor
@Entity
@Table(name = "payments")
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payment_id")
    private Long paymentId;

    @NotNull(message = "El ID de la orden es obligatorio")
    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @NotNull(message = "El monto es obligatorio")
    @Positive(message = "El monto debe ser un valor positivo")
    @Column(nullable = false)
    private Double monto; // Cambiar a BigDecimal si manejas decimales financieros estrictos

    @NotBlank(message = "El tipo de pago es obligatorio")
    @Column(name = "tipo_pago", nullable = false)
    private String tipoPago; // EJ: TARJETA_CREDITO, TRANSFERENCIA, PAYPAL

    @NotBlank(message = "El estado del pago es obligatorio")
    @Column(name = "estado_pago", nullable = false)
    private String estadoPago; // EJ: PENDIENTE, APROBADO, RECHAZADO, REEMBOLSADO

    @NotBlank(message = "El código o token de pago es obligatorio")
    @Column(name = "codigo_pago", nullable = false, unique = true)
    private String codigoPago; // Token de la pasarela (Webpay, Stripe, etc.)

    @NotNull(message = "La fecha de pago no puede ser nula")
    @PastOrPresent(message = "La fecha no puede ser futura")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "fecha_pago", nullable = false)
    private LocalDateTime fechaPago;

    @Embedded // SOLUCIÓN CRÍTICA: Requerido para que JPA reconozca las columnas de auditoría
    private Audit audit = new Audit();

    // Bloque que asegura que la fecha se asigne automáticamente si se envía vacía
    @PrePersist
    protected void onCreate() {
        if (this.fechaPago == null) {
            this.fechaPago = LocalDateTime.now();
        }
    }
}