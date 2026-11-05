package com.gamehubstore.authentificator_mscv.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// Datos que envia el cliente para iniciar sesion.
@Getter
@Setter
@NoArgsConstructor
public class LoginRequest {
    @NotBlank(message = "El email no puede estar vacío")
    @Email(message = "Debe ingresar un formato de correo válido")
    private String email;

    @NotBlank(message = "La contraseña no puede estar vacía")
    private String password;
}
