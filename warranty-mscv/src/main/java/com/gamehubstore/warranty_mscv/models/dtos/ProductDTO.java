package com.gamehubstore.warranty_mscv.models.dtos;


import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ProductDTO {
    @JsonAlias("idProduct")
    private Long productId;
    @JsonAlias("nombreProduct")
    private String nombre;
    private Integer mesesGarantia; // Clave para saber el plazo límite del producto
}