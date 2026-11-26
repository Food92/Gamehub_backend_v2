package com.gamehubstore.warranty_mscv.models.dtos;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ResolverGarantiaDTO {
    @NotBlank(message = "El estado es obligatorio")
    private String estado;
    private String diagnostico;
    private String resolucion;
}
