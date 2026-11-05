package com.gamehubstore.inventory_mscv.models.dtos;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
@NoArgsConstructor
public class InventoryDTO {

    private int inventarioId;

    @NotNull(message = "El ID del producto es obligatorio")
    private Long idProducto;

    @NotNull(message = "El stock disponible no puede ser nulo")
    @Min(value = 0, message = "El stock disponible no puede ser negativo") // Regla de negocio: No negativo
    private Long stockDisponible;

    @NotNull(message = "El stock reservado no puede ser nulo")
    @Min(value = 0, message = "El stock reservado no puede ser negativo")
    private Long stockReservado;

    @NotNull(message = "El stock mínimo es obligatorio para alertas de reposición")
    @Min(value = 0, message = "El stock mínimo no puede ser negativo")
    private Long stockMinimo;

    @NotBlank(message = "La ubicación en bodega es obligatoria (ej: Pasillo A - Estante 4)")
    private String ubicacion;
}