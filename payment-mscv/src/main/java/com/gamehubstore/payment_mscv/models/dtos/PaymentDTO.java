package com.gamehubstore.payment_mscv.models.dtos;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Setter
@Getter
@ToString
@NoArgsConstructor
public class PaymentDTO {

    @NotNull(message = "El ID de la orden (orderId) es obligatorio")
    private Long orderId;

    @NotNull(message = "El monto es obligatorio") // CORRECCIÓN: Evita que el valor viaje nulo
    @Positive(message = "El monto debe ser un valor positivo mayor a 0")
    private Double monto;

    @NotBlank(message = "El tipo de pago es obligatorio")
    private String tipoPago; // EJ: TARJETA_CREDITO, TRANSFERENCIA, PAYPAL

    @NotBlank(message = "El estado del pago es obligatorio")
    private String estadoPago; // EJ: PENDIENTE, APROBADO, RECHAZADO, REEMBOLSADO

    // No lleva anotaciones de validación porque el cliente no lo envía en el POST,
    // pero viaja lleno en el JSON de respuesta cuando el servicio lo genera.
    private String codigoPago;

    @NotNull(message = "La fecha de pago no puede ser nula")
    @PastOrPresent(message = "La fecha de pago no puede ser futura")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime fechaPago; // ALINEADO: Mismo nombre que en la entidad Payment para mapeo directo
}