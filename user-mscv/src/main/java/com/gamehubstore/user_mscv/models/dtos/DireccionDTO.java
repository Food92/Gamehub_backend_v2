package com.gamehubstore.user_mscv.models.dtos;

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
public class DireccionDTO {

    private Long direccionId;

    // El ID del usuario es obligatorio para amarrar la dirección en la BD
    @NotNull(message = "El campo userId no puede ser nulo")
    private Long userId;

    @NotBlank(message = "El campo comuna no puede ser vacio")
    private String comuna;

    @NotBlank(message = "El campo ciudad no puede ser vacio")
    private String ciudad;

    @NotBlank(message = "El campo calle no puede ser vacio")
    private String calle;

    @NotBlank(message = "El campo número no puede ser vacio")
    private String numero;
}