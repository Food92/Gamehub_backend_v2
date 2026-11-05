package com.gamehubstore.inventory_mscv.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
@NoArgsConstructor
@Entity
@Table(name = "inventarios")

public class Inventory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int inventarioId;

    @Column(nullable = false)
    @NotNull(message = "El campo de cantidad no puede ser vacio")
    private Long idProducto;

    @Column(nullable = false)
    @NotNull(message = "El campo de cantidad no puede ser vacio")
    private Long stockDisponible;


    @Column(nullable = false)
    @NotNull(message = "El campo de cantidad no puede ser vacio")
    private Long stockReservado;


    @Column(nullable = false)
    @NotNull(message = "El campo de cantidad no puede ser vacio")
    private Long stockMinimo;


    @Column(nullable = false)
    @NotBlank(message = "El campo de cantidad no puede ser vacio")
    private String ubicacion;

    public enum EstadoProducto {
        ACTIVO,
        INACTIVO
    }

    @Embedded
    private Audit audit = new Audit();
}