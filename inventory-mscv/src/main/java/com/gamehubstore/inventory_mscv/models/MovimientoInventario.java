package com.gamehubstore.inventory_mscv.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Setter
@Getter
@ToString
@NoArgsConstructor
@Entity
@Table(name = "movimientos_inventario")

public class MovimientoInventario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long movimientoInventarioId;

    @Column(nullable = false)
    @NotNull(message = "El campo de Idproducto no puede ser vacio")
    private Long idProducto;

    @Column(nullable = false)
    @NotBlank(message = "El campo de tipo no puede ser vacio")
    private String tipo; // Entrada, Salida, Reserva, Ajuste

    @Column(name = "order_id")
    private Long orderId; // Clave de idempotencia para movimientos de una saga.

    @Column(nullable = false)
    @NotNull(message = "El campo de cantidad no puede ser vacio")
    private Long cantidad;

    @Column(nullable = false)
    private LocalDateTime fecha;

    @Embedded
    private Audit audit = new Audit();
}