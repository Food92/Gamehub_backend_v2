package com.gamehubstore.category_mscv.models.dtos;

import com.gamehubstore.category_mscv.models.Category;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CategoryCountDTO {
    private Long idCategory;
    private String nombreCategory;
    private String descripcionCategory;
    private Category.EstadoCategory estado;
    private Long cantidadProductos;
}
