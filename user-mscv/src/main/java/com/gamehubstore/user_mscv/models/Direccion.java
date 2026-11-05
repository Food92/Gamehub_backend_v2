package com.gamehubstore.user_mscv.models;

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

public class Direccion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long direccionId;

    @NotNull(message = "El campo de userId no puede ser vacio")
    @Column(nullable = false)
    private Long userId;

    @NotBlank(message = "El campo de comuna no puede ser vacio")
    @Column(nullable = false)
    private String comuna;

    @NotBlank(message = "El campo de cieudad no puede ser vacio")
    @Column(nullable = false)
    private String ciudad;

    @NotBlank(message = "El campo de calle no puede ser vacio")
    @Column(nullable = false)
    private String calle;

    @NotNull(message = "El campo de numero no puede ser vacio")
    @Column(nullable = false)
    private String numero;
}
