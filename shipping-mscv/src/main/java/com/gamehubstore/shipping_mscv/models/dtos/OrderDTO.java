package com.gamehubstore.shipping_mscv.models.dtos;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class OrderDTO {

    private Long orderId; // El ID de la orden en el otro servicio
    private String estado; // El campo clave para validar si es "PAGADA"
}