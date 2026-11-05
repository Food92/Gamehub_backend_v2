package com.gamehubstore.warranty_mscv.models.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Estructura unificada para respuestas de error en la plataforma")
public class ErrorDetails {
        private String codigo;
        private String mensaje;
        private String detalles;
        private LocalDateTime timestamp;
}
