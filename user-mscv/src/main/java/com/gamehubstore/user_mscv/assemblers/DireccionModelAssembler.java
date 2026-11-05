package com.gamehubstore.user_mscv.assemblers;

import com.gamehubstore.user_mscv.controllers.DireccionControllerV2; // Tu clase real
import com.gamehubstore.user_mscv.controllers.UserControllerV2;      // Tu clase real
import com.gamehubstore.user_mscv.models.dtos.DireccionDTO;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@Component
public class DireccionModelAssembler implements RepresentationModelAssembler<DireccionDTO, EntityModel<DireccionDTO>> {

    @Override
    public EntityModel<DireccionDTO> toModel(DireccionDTO dto) {
        return EntityModel.of(dto,
                // Enlaces propios de la dirección usando tus controladores reales V2
                linkTo(methodOn(DireccionControllerV2.class).findByUserId(dto.getUserId())).withSelfRel(),
                linkTo(methodOn(DireccionControllerV2.class).delete(dto.getDireccionId())).withRel("delete-address"),

                // Enlace Relacional hacia el usuario dueño
                linkTo(methodOn(UserControllerV2.class).findById(dto.getUserId())).withRel("user-owner")
        );
    }
}