package com.gamehubstore.payment_mscv.models.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class SimularPagoRequest {

    @NotNull(message = "El ID de la orden (orderId) es obligatorio")
    @Schema(description = "ID de la orden en estado PENDIENTE_PAGO", example = "1")
    private Long orderId;

    @Schema(description = "Medio de pago simulado", example = "TARJETA_CREDITO", allowableValues = {"TARJETA_CREDITO", "TARJETA_DEBITO", "TRANSFERENCIA", "WEBPAY_SIMULADO"})
    private String tipoPago = "TARJETA_CREDITO";

    @Schema(description = "Monto opcional; si se omite, se toma automáticamente el total exacto de la orden")
    private Double monto;

    @Schema(description = "Si es true, simula un rechazo bancario en vez de aprobación", example = "false")
    private boolean simularRechazo = false;
}
