package com.gamehubstore.inventory_mscv.models.dtos;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class MovimientoRequestDTO {
    @NotNull(message = "El ID del producto es obligatorio")
    private Long idProducto;

    @NotBlank(message = "El tipo de movimiento es obligatorio (INGRESO, SALIDA, AJUSTE)")
    private String tipo;

    @NotNull(message = "La cantidad es obligatoria")
    @Min(value = 1, message = "La cantidad debe ser mayor o igual a 1")
    private Long cantidad;

    private String motivo;
}
