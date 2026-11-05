package com.gamehubstore.product_mscv.assemblers;

import com.gamehubstore.product_mscv.controllers.ProductControllerV2;
import com.gamehubstore.product_mscv.models.dtos.ProductDTO;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@Component
public class ProductModelAssembler implements RepresentationModelAssembler<ProductDTO, EntityModel<ProductDTO>> {

    @Override
    public EntityModel<ProductDTO> toModel(ProductDTO productDTO) {
        // Creamos el EntityModel envolviendo al DTO y le agregamos los enlaces dinámicos
        return EntityModel.of(productDTO,
                linkTo(methodOn(ProductControllerV2.class).findById(productDTO.getIdProduct())).withSelfRel(),
                linkTo(methodOn(ProductControllerV2.class).findAll()).withRel("products")
        );
    }
}