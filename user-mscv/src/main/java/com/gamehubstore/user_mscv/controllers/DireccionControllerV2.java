package com.gamehubstore.user_mscv.controllers;

import com.gamehubstore.user_mscv.assemblers.DireccionModelAssembler;
import com.gamehubstore.user_mscv.models.dtos.DireccionDTO;
import com.gamehubstore.user_mscv.services.DireccionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Tag(name = "Direcciones V2", description = "Endpoints CRUD de localización optimizados con HATEOAS")
@RestController
@Validated
@RequestMapping("/api/v2/addresses")
public class DireccionControllerV2 {

    @Autowired
    private DireccionService direccionService;

    @Autowired
    private DireccionModelAssembler direccionModelAssembler;

    @PostMapping
    @Operation(summary = "Registrar una nueva dirección de despacho - V2")
    public ResponseEntity<EntityModel<DireccionDTO>> save(@Valid @RequestBody DireccionDTO direccionDTO) {
        DireccionDTO savedDto = direccionService.save(direccionDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(direccionModelAssembler.toModel(savedDto));
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Listar direcciones de un usuario - V2")
    public ResponseEntity<CollectionModel<EntityModel<DireccionDTO>>> findByUserId(
            @Parameter(description = "ID único del usuario para consultar sus direcciones", required = true, example = "1")
            @PathVariable Long userId
    ) {
        List<EntityModel<DireccionDTO>> addresses = direccionService.findByUserId(userId)
                .stream()
                .map(direccionModelAssembler::toModel)
                .toList();

        CollectionModel<EntityModel<DireccionDTO>> collectionModel = CollectionModel.of(
                addresses,
                linkTo(methodOn(DireccionControllerV2.class).findByUserId(userId)).withSelfRel()
        );

        return ResponseEntity.ok(collectionModel);
    }

    @DeleteMapping("/{direccionId}")
    @Operation(summary = "Eliminar dirección por ID - V2")
    public ResponseEntity<Void> delete(
            @Parameter(description = "ID único de la dirección específica a remover", required = true, example = "10")
            @PathVariable Long direccionId
    ) {
        direccionService.delete(direccionId);
        return ResponseEntity.noContent().build();
    }
}