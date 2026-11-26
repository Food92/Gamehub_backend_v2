package com.gamehubstore.promotion_mscv.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CuponValidacionDTO {
    private boolean valido;
    private String codigo;
    private String tipo;
    private BigDecimal valor;
    private BigDecimal descuento;
    private String mensaje;
}
