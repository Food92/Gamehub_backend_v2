package com.gamehubstore.inventory_mscv.models.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StockAvailabilityDTO {
    private Long idProducto;
    private Long stockDisponible;
    private Long stockReservado;
    private boolean disponible;
}
