package com.gamehubstore.inventory_mscv.models.dtos;


import com.gamehubstore.inventory_mscv.models.Inventory;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
@NoArgsConstructor
public class ProductDTO {
    private Long idProduct;
    private String nombreProduct;
    private String marca;
    private String modelo;
    private Double precio;
    private  Long idCategory;
    private String descripcion;
    private Boolean estado;
}
