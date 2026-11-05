package com.gamehubstore.shipping_mscv.models.dtos; // 👈 Debe terminar en .dtos

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class CancelShippingDTO {
    private Long shippingId;
    private String motivo;
}