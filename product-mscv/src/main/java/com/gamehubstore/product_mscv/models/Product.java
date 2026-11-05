package com.gamehubstore.product_mscv.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
@NoArgsConstructor
@Entity
@Table(name = "products")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_product")
    private Long idProduct;

    @NotBlank(message = "El campo nombre de product no puede ser vacio")
    @Column(nullable = false)
    private String nombreProduct;

    @NotBlank(message = "El campo marca no puede ser vacio")
    @Column(nullable = false)
    private String marca;

    @NotBlank(message = "El campo modelo no puede ser vacio")
    @Column(nullable = false)
    private String modelo;

    @NotNull(message = "El campo precio no puede ser vacio")
    @Column(nullable = false)
    private Double precio;

    @NotNull(message = "El campo idCategory no puede ser vacio")
    @Column(nullable = false)
    private  Long idCategory;

    @NotBlank(message = "El campo descripcion no puede ser vacio")
    @Column(nullable = false)
    private String descripcion;

    @NotNull @Min(1)
    @Column(nullable = false)
    private Integer mesesGarantia = 12;

    @NotNull(message = "El estado no puede ser nulo")
    @Column(nullable = false)
    private Boolean estado;

    @Embedded
    private Audit audit=new Audit();

    @Column(name = "imagen_url", length = 1000)
    private String imagenUrl;
}
