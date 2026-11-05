package com.gamehubstore.authentificator_mscv.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;

// Vista segura de un usuario (sin la contrasena).
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CuentaAccesoDTO {
    private Long usuarioId;
    private String username;
    private String rol;
}
