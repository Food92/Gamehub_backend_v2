package com.gamehubstore.user_mscv.models;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Embeddable
@Getter
@Setter
@ToString
@NoArgsConstructor
public class Audit {

    @Column(name = "created_at")
    private LocalDate createdAt;

    @Column(name = "updated_at")
    private LocalDate updatedAt;

    /**
     * Este método se ejecuta automaticamente una vez que el objeto es creado
     */
    @PrePersist
    public void prePersist(){
        this.createdAt = LocalDate.now();
    }

    /**
     * Este método se ejecuta automaticamente cuando se realiza cualquier actu
     * lización del objeto que se encuentra asociado.
     */
    @PreUpdate
    public void preUpdate(){
        this.updatedAt = LocalDate.now();
    }
}