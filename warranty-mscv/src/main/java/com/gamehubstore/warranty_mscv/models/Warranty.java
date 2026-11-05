package com.gamehubstore.warranty_mscv.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "garantias")
@Getter
@Setter
@NoArgsConstructor
public class Warranty {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "usuario_id", nullable = false)
    private Long userId;

    @Column(name = "orden_id", nullable = false)
    private Long ordenId;

    @Column(name = "producto_id", nullable = false)
    private Long productId;

    @Column(nullable = false, length = 500)
    private String motivo;

    @Column(nullable = false, length = 50)
    private String estado; // CREADO, EN_REVISION, REPARACION, CAMBIO, CERRADO

    @Column(name = "fecha_solicitud", nullable = false)
    private LocalDateTime fechaSolicitud;

    @Column(length = 1000)
    private String resolucion;

    @Column(length = 1000)
    private String diagnostico;

    @PrePersist
    protected void onCreate() {
        this.fechaSolicitud = LocalDateTime.now();
        if (this.estado == null) {
            this.estado = "CREADO";
        }
    }
}