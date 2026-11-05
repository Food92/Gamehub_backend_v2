package com.gamehubstore.product_mscv.models.dtos;

import com.gamehubstore.product_mscv.models.Audit;
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
    private Audit audit = new Audit();
    private String imagenUrl;
}
