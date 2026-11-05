package com.gamehubstore.authentificator_mscv.dtos;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;

// Respuesta del login/registro: el token JWT y datos basicos del usuario.
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    private String token;
    private String tokenType; // "Bearer"
    private Long id;
    private String email;
    private String rol;
}
