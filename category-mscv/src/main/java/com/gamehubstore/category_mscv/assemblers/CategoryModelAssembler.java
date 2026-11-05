package com.gamehubstore.category_mscv.assemblers;


import com.gamehubstore.category_mscv.controllers.ControllerCategoryV2;
import com.gamehubstore.category_mscv.models.Category;
import com.gamehubstore.category_mscv.models.dtos.CategoryDTO;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@Component
public class CategoryModelAssembler {

    // Vincula el DTO comercial con los enlaces técnicos usando el ID de la Entidad
    public EntityModel<CategoryDTO> toModel(Category entity, CategoryDTO dto) {
        return EntityModel.of(dto,
                linkTo(methodOn(ControllerCategoryV2.class).findById(entity.getIdCategory())).withSelfRel(),
                linkTo(methodOn(ControllerCategoryV2.class).findAll()).withRel("categories")
        );
    }

}