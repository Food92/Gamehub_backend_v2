package com.gamehubstore.order_mscv.models.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LineaValidadaDTO {
    private Long idProducto;
    private String nombreProducto;
    private Integer cantidadSolicitada;
    private Long stockDisponible;
    private Double precioUnitario;
    private Double subtotal;
    private boolean disponible;
    private String observacion;
}
