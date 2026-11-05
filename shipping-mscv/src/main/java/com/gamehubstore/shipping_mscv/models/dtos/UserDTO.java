package com.gamehubstore.shipping_mscv.models.dtos;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class UserDTO {

    private Long userId; // El ID del usuario en el user-service (a veces llamado 'id')
    private String direccion; // Campo clave para validar la logística del despacho
}