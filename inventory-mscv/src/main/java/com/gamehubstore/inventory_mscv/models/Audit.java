package com.gamehubstore.inventory_mscv.models;

import jakarta.persistence.Column;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;

@ToString
@Setter
@Getter
@NoArgsConstructor
public class Audit {

    @Column(name = "creates_at")
    private LocalDate createdAt;

    @Column(name = "updated_at")
    private LocalDate updatedAt;

    /**
     * Este método se ejecuta automaticamente una vez que el objeto es creado
     */

    @PrePersist
    public void prePersist() {
        createdAt = LocalDate.now();
    }

    /**
     * Este método se ejecuta automaticamente cuando se realiza cualquier actu
     * lización del objeto que se encuentra asociado.
     */

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDate.now();
    }
}