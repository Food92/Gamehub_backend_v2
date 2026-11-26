package com.gamehubstore.authentificator_mscv.dtos;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ActivoRequest {
    @NotNull(message = "El estado activo es obligatorio")
    private Boolean activo;
}
