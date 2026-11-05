package com.gamehubstore.order_mscv.models;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Embeddable
@Setter
@Getter
@ToString
@NoArgsConstructor
public class DetailOrder {

    @NotNull(message = "El idProducto es obligatorio")
    private Long idProducto;

    @NotNull(message = "La cantidad no puede ser nula")
    @Min(value = 1, message = "La cantidad mínima debe ser al menos 1")
    private Integer cantidad;

    @NotNull(message = "El precio unitario es obligatorio")
    @Positive(message = "El precio debe ser mayor a 0")
    private Double precioUnitario;

    @NotNull(message = "El precio total del ítem es obligatorio")
    @Positive(message = "El total por ítem debe ser mayor a 0")
    private Double precioTotal; // cantidad * precioUnitario
}