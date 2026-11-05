package com.gamehubstore.payment_mscv.models;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Embeddable
@Getter
@Setter
@ToString
@NoArgsConstructor
public class Audit {

    // updatable = false garantiza que el UPDATE de SQL jamás altere o borre la fecha de creación
    @Column(name = "created_at", updatable = false)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") // Opcional: Da un formato limpio al JSON devuelto
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    /**
     * Este método se ejecuta automáticamente una vez que el objeto es creado
     * en la base de datos (Operación INSERT).
     */
    @PrePersist
    public void prePersist(){
        this.createdAt = LocalDateTime.now();
    }

    /**
     * Este método se ejecuta automáticamente cuando se realiza cualquier
     * actualización del objeto asociado (Operación UPDATE).
     */
    @PreUpdate
    public void preUpdate(){
        this.updatedAt = LocalDateTime.now();
    }
}