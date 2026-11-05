package com.gamehubstore.shipping_mscv.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@ToString
@Entity
@Table(name = "shipping")
public class Shipping {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // 👈 Recomendado IDENTITY en lugar de AUTO para evitar problemas de secuencias compartidas en MySQL/PostgreSQL
    private Long shippingId;

    @NotNull(message = "El ID de la orden es obligatorio")
    private Long orderId;

    @NotNull(message = "El ID del usuario es obligatorio")
    private Long userId;

    @NotBlank(message = "La direccion es obligatoria")
    private String direccion;

    @NotBlank(message = "El transportista es obligatorio")
    private String transportista;

    @Column(unique = true)
    private String tracking;

    @Column(nullable = false) // 👈 Quitamos @NotBlank de aquí para que el @PrePersist pueda inicializarlo automáticamente si no lo mandas en el JSON
    private String estado; // CREADO, EN_TRANSITO, ENTREGADO, CANCELADO

    private LocalDate fechaEnvio;

    private LocalDate fechaEntrega; // 👈 Agregado para cumplir con la regla de negocio: "No cambiar a entregado sin fecha de entrega"

    @PrePersist
    protected void onCreate() {
        if (this.estado == null || this.estado.trim().isEmpty()) {
            this.estado = "CREADO"; // 👈 Regla: Estado inicial automático al persistir
        }
        if (this.fechaEnvio == null) {
            this.fechaEnvio = LocalDate.now(); // 👈 Asigna la fecha actual si no se especifica
        }
    }
}