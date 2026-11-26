package com.gamehubstore.order_mscv.models.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ResumenOrdenesDTO {
    private long totalOrdenes;
    private Map<String, Long> porEstado;
    private Double montoTotalPagado;
    private long pendientesPago;
    private long enDespacho;
}
