package com.gamehubstore.authentificator_mscv.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "cuentas_acceso")
@Getter
@Setter
@NoArgsConstructor
public class CuentaAcceso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @NotBlank(message = "El email no puede estar vacío")
    @Email(message = "El formato del email no es válido")
    @Column(unique = true, nullable = false)
    private String email;

    // Se guarda CIFRADA con BCrypt, nunca en texto plano.
    @NotBlank(message = "La contraseña no puede estar vacía")
    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    // Simplificación de Arquitectura para GameHub Store:
    // En lugar de una tabla intermedia ManyToMany pesada, usamos un String directo
    // que almacena el rol jerárquico (ROLE_CLIENTE, ROLE_OPERADOR, ROLE_ADMIN).
    @NotBlank(message = "El rol no puede estar vacío")
    @Column(nullable = false)
    private String rol;

    // Atributo obligatorio de control lógico: Usuario inactivo no puede autenticarse
    @Column(nullable = false)
    private Boolean estado = true;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;

    @PrePersist
    protected void onCreate() {
        this.fechaCreacion = LocalDateTime.now();
        if (this.estado == null) {
            this.estado = true;
        }
    }
}