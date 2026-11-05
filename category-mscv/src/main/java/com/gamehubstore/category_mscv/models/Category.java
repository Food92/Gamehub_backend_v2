package com.gamehubstore.category_mscv.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
@Entity
@NoArgsConstructor
@Table(name = "categories")
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idCategory;

    @NotBlank
    @Column(nullable = false)
    private String nombreCategory;

    @NotBlank
    @Column(nullable = false)
    private String descripcionCategory;

    @Enumerated(EnumType.STRING)
    private EstadoCategory estado;

    public enum EstadoCategory {
        ACTIVO,
        INACTIVO
    }

}
