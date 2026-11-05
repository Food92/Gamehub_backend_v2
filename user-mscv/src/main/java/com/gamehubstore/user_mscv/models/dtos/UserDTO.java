package com.gamehubstore.user_mscv.models.dtos;

import jakarta.persistence.Column;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
@NoArgsConstructor
public class UserDTO {
    private Long userId;

    @NotBlank(message = "El campo rut no puede ser vacio")
    @Pattern(regexp = "^\\d{7,8}-[\\dkK]$", message = "El formato del run debe ser xxxxxxxx-x")
    private String rut;

    @NotBlank(message = "El nombre no puede ser vacio")
    private String nombreCompleto;

    @NotBlank(message = "El apellido no puede ser vacio")
    private String apellidoCompleto;

    @NotBlank(message = "El campo de correo no puede ser vacio")
    @Email(message = "El campo de correo tiene que tener el formato de correo")
    private String correo;


    @NotBlank(message = "El campo de telefono no puede ser vaco")
    @Pattern(
            regexp = "^(\\+56)?\\s?9\\d{8}$",
            message = "El formato del teléfono debe ser +56 9XXXXXXXX")
    private String telefono;

    private Boolean estado;
}