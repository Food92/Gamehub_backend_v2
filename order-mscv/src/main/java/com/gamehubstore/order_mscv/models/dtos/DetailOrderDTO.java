package com.gamehubstore.order_mscv.models.dtos;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
@NoArgsConstructor
public class DetailOrderDTO {

    @NotNull(message = "El idProducto es obligatorio en el detalle")
    private Long idProduct;

    @NotNull(message = "La cantidad no puede ser nula")
    @Min(value = 1, message = "La cantidad mínima de un producto debe ser 1")
    private Long cantidad;

    private Double precioUnitario; // Se toma del catálogo; el cliente no define el precio.

    private Double precioTotal; // Se calcula en el servicio
}