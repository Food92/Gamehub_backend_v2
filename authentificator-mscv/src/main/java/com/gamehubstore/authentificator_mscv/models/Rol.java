package com.gamehubstore.authentificator_mscv.models;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// Tabla de roles. El nombre se guarda con el prefijo ROLE_ (convencion de Spring Security):
// ROLE_ADMIN, ROLE_MEDICO, ROLE_PACIENTE.
@Entity
@Table(name = "roles")
@Getter
@Setter
@NoArgsConstructor
public class Rol {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "rol_id")
    private Long rolId;

    @Column(unique = true, nullable = false)
    private String nombre;

    public Rol(String nombre) {
        this.nombre = nombre;
    }
}