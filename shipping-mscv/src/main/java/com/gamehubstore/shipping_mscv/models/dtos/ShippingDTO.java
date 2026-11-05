package com.gamehubstore.shipping_mscv.models.dtos;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class ShippingDTO {

    private Long shippingId;
    private Long orderId;
    private Long userId;
    private String direccion;
    private String transportista;
    private String tracking;
    private String estado;
    private LocalDate fechaEnvio;
    private LocalDate fechaEntrega;
}