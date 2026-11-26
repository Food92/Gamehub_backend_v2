package com.gamehubstore.order_mscv.models.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ValidacionCarritoDTO {
    private boolean valido;
    private Double subtotal;
    private Double descuento;
    private Double total;
    private Boolean cuponValido;
    private String mensajeCupon;
    private List<LineaValidadaDTO> items;
}
