package com.gamehubstore.payment_mscv.models.dtos;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
@NoArgsConstructor
public class OrderClientDTO {

    private Long orderId;
    private Long userId;
    private Double total;       // El campo más importante para el pago
    private String estado;      // EJ: CREADA, PAGADA, CANCELADA
}