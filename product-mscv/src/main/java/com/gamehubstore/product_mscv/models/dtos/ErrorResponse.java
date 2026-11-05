package com.gamehubstore.product_mscv.models.dtos;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Setter
@Getter
@ToString
@NoArgsConstructor
public class ErrorResponse {
    private String codigo;
    private String mensaje;
    private String detalles;
    private LocalDateTime timestamp;

}
