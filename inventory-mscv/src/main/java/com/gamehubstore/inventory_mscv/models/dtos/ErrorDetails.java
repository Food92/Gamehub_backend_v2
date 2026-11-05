package com.gamehubstore.inventory_mscv.models.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ErrorDetails {
    private String codigo;
    private String mensaje;
    private String detalles;
    private LocalDateTime timestamp;}
