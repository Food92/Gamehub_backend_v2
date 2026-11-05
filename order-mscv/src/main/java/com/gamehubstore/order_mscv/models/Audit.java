package com.gamehubstore.order_mscv.models;

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
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * Este método se ejecuta automaticamente una vez que el objeto es creado
     */
    @PrePersist
    public void prePersist(){
        this.createdAt = LocalDateTime.now();
    }

    /**
     * Este método se ejecuta automaticamente cuando se realiza cualquier actu
     * lización del objeto que se encuentra asociado.
     */
    @PreUpdate
    public void preUpdate(){
        this.updatedAt = LocalDateTime.now();
    }
}