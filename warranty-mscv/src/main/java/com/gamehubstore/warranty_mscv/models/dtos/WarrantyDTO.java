package com.gamehubstore.warranty_mscv.models.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class WarrantyDTO {
    private Long id;

    @NotNull(message = "El ID de usuario es obligatorio")
    private Long usuarioId;

    @NotNull(message = "El ID de la orden es obligatorio")
    private Long ordenId;

    @NotNull(message = "El ID del producto es obligatorio")
    private Long productoId;

    @NotBlank(message = "El motivo de la garantía no puede estar vacío")
    private String motivo;

    private String estado; // CREADO, EN_REVISION, ACEPTADO, RECHAZADO, CERRADO
    private String resolucion;
}