package com.gamehubstore.warranty_mscv.models.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ResumenGarantiasDTO {
    private long total;
    private Map<String, Long> porEstado;
    private long abiertas;
    private long cerradas;
}
