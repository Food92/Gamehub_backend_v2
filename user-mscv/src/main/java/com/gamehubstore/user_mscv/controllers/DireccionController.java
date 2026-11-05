package com.gamehubstore.user_mscv.controllers;

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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Direcciones V1", description = "Endpoints para la gestión de direcciones de despacho asociadas a los usuarios")
@RestController
@Validated
@RequestMapping("/api/v1/users/{userId}/direcciones")
public class DireccionController {

    @Autowired
    private DireccionService direccionService;

    // Crear dirección para un usuario
    @PostMapping
    @Operation(
            summary = "Crear dirección para un usuario",
            description = "Registra una nueva dirección de despacho asignándola obligatoriamente al ID de usuario provisto en la URL"
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Estructura de la dirección de despacho a registrar", required = true,
            content = @Content(schema = @Schema(implementation = DireccionDTO.class))
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Dirección registrada con éxito"),
            @ApiResponse(responseCode = "400", description = "Los datos ingresados para la dirección no son válidos")
    })
    public ResponseEntity<DireccionDTO> save(
            @Parameter(description = "ID único del usuario dueño de la dirección", required = true, example = "1")
            @PathVariable Long userId,
            @Valid @RequestBody DireccionDTO direccionDTO
    ) {
        direccionDTO.setUserId(userId); // Asignar el userId desde la URL de forma segura
        DireccionDTO saved = direccionService.save(direccionDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    // Listar direcciones de un usuario
    @GetMapping
    @Operation(
            summary = "Listar direcciones de un usuario",
            description = "Retorna el historial completo de todas las ubicaciones y domicilios de entrega registrados por el cliente"
    )
    @ApiResponse(responseCode = "200", description = "Operación Exitosa")
    public ResponseEntity<List<DireccionDTO>> listarDirecciones(
            @Parameter(description = "ID único del usuario para consultar sus direcciones", required = true, example = "1")
            @PathVariable Long userId
    ) {
        List<DireccionDTO> direcciones = direccionService.findByUserId(userId);
        return ResponseEntity.ok(direcciones);
    }

    // Eliminar dirección por ID
    @DeleteMapping("/{direccionId}")
    @Operation(
            summary = "Eliminar dirección por ID",
            description = "Remueve permanentemente una dirección física específica del sistema de despacho"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Dirección eliminada correctamente (No Content)"),
            @ApiResponse(
                    responseCode = "404",
                    description = "La dirección solicitada con ese ID no existe en la BD",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(type = "object", example = "{\"codigo\": \"NOT_FOUND\", \"mensaje\": \"La dirección solicitada no existe.\", \"detalles\": \"uri=/api/v1/users/1/direcciones/\"}")
                    )
            )
    })
    public ResponseEntity<Void> eliminarDireccion(
            @Parameter(description = "ID único de la dirección específica a remover", required = true, example = "10")
            @PathVariable Long direccionId
    ) {
        direccionService.delete(direccionId);
        return ResponseEntity.noContent().build(); // Buenas prácticas REST: devuelve HttpStatus 204 sin cuerpo
    }
}