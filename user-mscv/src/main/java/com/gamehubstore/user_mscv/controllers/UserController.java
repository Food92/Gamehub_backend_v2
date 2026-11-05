package com.gamehubstore.user_mscv.controllers;

import com.gamehubstore.user_mscv.models.User;
import com.gamehubstore.user_mscv.models.dtos.UserDTO;
import com.gamehubstore.user_mscv.services.UserService;
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

@Tag(name = "Usuarios V1", description = "Metodos CRUD para la gestión de usuarios, perfiles y estados")
@RestController
@Validated
@RequestMapping("/api/v1/users")
public class UserController {

    @Autowired
    private UserService userService;

    // Buscar usuario por ID
    @GetMapping("/{userId}")
    @Operation(summary = "Búsqueda de un usuario por ID", description = "Se devuelve un usuario en base a su ID, en caso contrario lanza una excepción")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuario encontrado con éxito",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserDTO.class))),
            @ApiResponse(responseCode = "404", description = "El usuario buscado no existe en la BD",
                    content = @Content(mediaType = "application/json", schema = @Schema(type = "object", example = "{\"codigo\": \"NOT_FOUND\", \"mensaje\": \"El usuario solicitado no existe en el sistema.\", \"detalles\": \"uri=/api/v1/users/\"}")))
    })
    public ResponseEntity<User> findById(
            @Parameter(description = "ID único del usuario a buscar", required = true, example = "1")
            @PathVariable Long userId
    ) {
        User user = userService.findById(userId);
        return ResponseEntity.ok(user);
    }

    // Crear usuario
    // CORRECCIÓN: Se cambió la firma para recibir y retornar UserDTO mapeado con el UserService.java
    @PostMapping
    @Operation(summary = "Guardado de usuario", description = "Esta es la forma de registrar un nuevo usuario en la tienda")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Datos del usuario a registrar", required = true,
            content = @Content(schema = @Schema(implementation = UserDTO.class))
    )
    public ResponseEntity<UserDTO> crearUsuario(@Valid @RequestBody UserDTO userDTO) {
        UserDTO savedUser = userService.save(userDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedUser);
    }

    // Actualizar usuario
    @PutMapping("/{userId}")
    @Operation(summary = "Actualización de usuario", description = "Se actualizan los datos personales de un usuario existente")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Datos del usuario a actualizar", required = true,
            content = @Content(schema = @Schema(implementation = UserDTO.class))
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuario actualizado correctamente",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserDTO.class))),
            @ApiResponse(responseCode = "400", description = "Datos de actualización inválidos."),
            @ApiResponse(responseCode = "404", description = "El usuario que se intenta modificar no existe en la BD",
                    content = @Content(mediaType = "application/json", schema = @Schema(type = "object", example = "{\"codigo\": \"NOT_FOUND\", \"mensaje\": \"El usuario a modificar no fue encontrado.\", \"detalles\": \"uri=/api/v1/users/\"}")))
    })
    public ResponseEntity<User> update(
            @Parameter(description = "ID del usuario a actualizar", required = true, example = "1")
            @PathVariable Long userId,
            @Valid @RequestBody User user
    ) {
        User updatedUser = userService.update(userId, user);
        return ResponseEntity.ok(updatedUser);
    }

    // Eliminar usuario
    @DeleteMapping("/{userId}")
    @Operation(summary = "Eliminar permanentemente un usuario", description = "Remueve por completo el registro de la base de datos")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Usuario eliminado"),
            @ApiResponse(responseCode = "404", description = "El usuario no existe",
                    content = @Content(mediaType = "application/json", schema = @Schema(type = "object", example = "{\"codigo\": \"NOT_FOUND\", \"mensaje\": \"No se puede eliminar. El usuario no existe.\", \"detalles\": \"uri=/api/v1/users/\"}")))
    })
    public ResponseEntity<Void> deleteById(
            @Parameter(description = "ID del usuario a eliminar", required = true, example = "1")
            @PathVariable Long userId
    ) {
        userService.delete(userId);
        return ResponseEntity.noContent().build();
    }

    // Desactivar usuario
    @PatchMapping("/{userId}/deactivate")
    @Operation(summary = "Desactivar un usuario (Baja Lógica)", description = "Cambia el estado del usuario a inactivo para prevenir compras")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Usuario desactivado exitosamente"),
            @ApiResponse(responseCode = "404", description = "El usuario no fue encontrado",
                    content = @Content(mediaType = "application/json", schema = @Schema(type = "object", example = "{\"codigo\": \"NOT_FOUND\", \"mensaje\": \"No se puede desactivar. El usuario no existe.\", \"detalles\": \"uri=/api/v1/users/\"}")))
    })
    public ResponseEntity<Void> desactivarUsuario(
            @Parameter(description = "ID del usuario a desactivar", required = true, example = "1")
            @PathVariable Long userId
    ) {
        userService.desactivar(userId);
        return ResponseEntity.noContent().build();
    }

    // Listar todos los usuarios
    @GetMapping
    @Operation(summary = "Listado de todos los usuarios", description = "Se devuelve una lista con todos los usuarios sin importar su estado")
    @ApiResponse(responseCode = "200", description = "Operación Exitosa")
    public ResponseEntity<List<User>> listarUsuarios() {
        return ResponseEntity.ok(userService.findAll());
    }

    // Listar usuarios activos
    @GetMapping("/activos")
    @Operation(summary = "Listar usuarios activos", description = "Retorna una lista exclusiva de usuarios habilitados en el sistema")
    @ApiResponse(responseCode = "200", description = "Operación Exitosa")
    public ResponseEntity<List<User>> listarUsuariosActivos() {
        return ResponseEntity.ok(userService.findByEstadoTrue());
    }

    // Listar usuarios inactivos
    @GetMapping("/inactivos")
    @Operation(summary = "Listar usuarios inactivos", description = "Retorna la lista de usuarios dados de baja o deshabilitados")
    @ApiResponse(responseCode = "200", description = "Operación Exitosa")
    public ResponseEntity<List<User>> listarUsuariosInactivos() {
        return ResponseEntity.ok(userService.findByEstadoFalse());
    }
}