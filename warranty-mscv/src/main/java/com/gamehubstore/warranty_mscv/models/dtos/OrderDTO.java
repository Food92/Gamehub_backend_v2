package com.gamehubstore.warranty_mscv.models.dtos;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class OrderDTO {
    private Long orderId;
    @JsonAlias("userId")
    private Long usuarioId;
    private java.util.List<DetalleOrdenDTO> details;
    private String estado;         // Debe estar en "PAGADA" o "ENTREGADA"
    @JsonAlias("fecha")
    private LocalDateTime fechaCompra;

    @Getter @Setter @NoArgsConstructor
    public static class DetalleOrdenDTO { @JsonAlias("idProducto") private Long productoId; } // Clave para validar el plazo de garantía
}
