package com.gamehubstore.order_mscv.models.dtos;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.List;

@Setter
@Getter
@ToString
@NoArgsConstructor
public class OrderDTO {

    private Long orderId;

    @NotNull(message = "El idUsuario es obligatorio")
    private Long idUsuario; // Cambiado para coincidir con la implementación del service

    private Double descuento; // Se calcula en el servicio

    private String estado; // Lo define el servicio al crear una orden.

    private Double subtotal; // Se calcula en el servicio

    private Double total; // Se calcula en el servicio

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime fechaCreacion; // Añadido para capturar la fecha que retorna el servicio

    @Size(max = 40, message = "La orden no puede superar 40 líneas")
    @NotEmpty(message = "La orden debe contener al menos un detalle de producto")
    @Valid // CRÍTICO: Obliga a Spring a validar las restricciones internas de cada DetailOrderDTO
    private List<DetailOrderDTO> details;

    @Size(max = 40)
    private String promotionCode;
}