package com.gamehubstore.authentificator_mscv.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

// Datos para crear un usuario. 'roles' es opcional: si viene vacio se asigna ROLE_PACIENTE.
// Ejemplo: {"username":"dr.house","password":"1234","roles":["ROLE_MEDICO"]}
@Getter
@Setter
@NoArgsConstructor
public class RegisterRequest {
    @NotBlank(message = "El email no puede estar vacío")
    @Email(message = "Debe ingresar un formato de correo válido")
    private String email;

    @NotBlank
    private String password;

    private Boolean estado;
    private String rol;
}
