package com.gamehubstore.user_mscv.assemblers;


import com.gamehubstore.user_mscv.controllers.UserControllerV2;
import com.gamehubstore.user_mscv.models.dtos.UserDTO;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@Component
public class UserModelAssembler implements RepresentationModelAssembler<UserDTO, EntityModel<UserDTO>> {

    @Override
    public EntityModel<UserDTO> toModel(UserDTO dto) {
        // Enlaza el DTO comercial de Usuario con sus respectivas URLs técnicas de la API REST v2
        return EntityModel.of(dto,
                linkTo(methodOn(UserControllerV2.class).findById(dto.getUserId())).withSelfRel(),
                linkTo(methodOn(UserControllerV2.class).update(dto.getUserId(), null)).withRel("update-user"),
                linkTo(methodOn(UserControllerV2.class).desactivar(dto.getUserId())).withRel("desactivar-usuario"),
                linkTo(methodOn(UserControllerV2.class).findAll()).withRel("all-users")
        );
    }
}