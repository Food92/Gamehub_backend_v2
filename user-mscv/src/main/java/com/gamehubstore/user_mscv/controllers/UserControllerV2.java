package com.gamehubstore.user_mscv.controllers;

import com.gamehubstore.user_mscv.assemblers.UserModelAssembler;
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
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Tag(name = "Usuarios V2", description = "Métodos CRUD para la gestión de usuarios optimizados con HATEOAS e Hypermedia")
@RestController
@Validated
@RequestMapping("/api/v2/users")
public class UserControllerV2 {

    @Autowired
    private UserService userService;

    @Autowired
    private UserModelAssembler userModelAssembler;

    // Métodos Helper de conversión internos para aislar la capa del Modelo de la de Transporte
    private UserDTO convertToDto(User user) {
        if (user == null) return null;
        UserDTO dto = new UserDTO();
        dto.setUserId(user.getUserId());
        dto.setRut(user.getRut());
        dto.setNombreCompleto(user.getNombreCompleto());
        dto.setApellidoCompleto(user.getApellidoCompleto());
        dto.setCorreo(user.getCorreo());
        dto.setTelefono(user.getTelefono());
        dto.setEstado(user.getEstado());
        return dto;
    }

    private User convertToEntity(UserDTO dto) {
        if (dto == null) return null;
        User user = new User();
        user.setRut(dto.getRut());
        user.setNombreCompleto(dto.getNombreCompleto());
        user.setApellidoCompleto(dto.getApellidoCompleto());
        user.setCorreo(dto.getCorreo());
        user.setTelefono(dto.getTelefono());
        user.setEstado(dto.getEstado());
        return user;
    }

    // 1. Buscar usuario por ID
    @GetMapping("/{id}")
    @Operation(summary = "Búsqueda de un usuario por ID - V2", description = "Retorna el usuario envuelto con hipermedios HATEOAS")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuario encontrado con éxito"),
            @ApiResponse(responseCode = "404", description = "El usuario solicitado no existe")
    })
    public ResponseEntity<EntityModel<UserDTO>> findById(
            @Parameter(description = "ID único del usuario a buscar", required = true, example = "1")
            @PathVariable Long id
    ) {
        User user = userService.findById(id);
        UserDTO dto = this.convertToDto(user);
        return ResponseEntity.ok(userModelAssembler.toModel(dto));
    }

    // 2. Crear usuario
    @PostMapping
    @Operation(summary = "Guardado de usuario - V2", description = "Registra un usuario y retorna su representación con links de acción")
    @ApiResponse(responseCode = "201", description = "Usuario registrado con éxito")
    public ResponseEntity<EntityModel<UserDTO>> crearUsuario(@Valid @RequestBody UserDTO userDTO) {
        UserDTO savedUserDto = userService.save(userDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(userModelAssembler.toModel(savedUserDto));
    }

    // 3. Actualizar usuario
    @PutMapping("/{id}")
    @Operation(summary = "Actualización de usuario - V2", description = "Modifica los datos personales de un usuario usando estructuras DTO")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuario actualizado correctamente"),
            @ApiResponse(responseCode = "400", description = "Datos de entrada no válidos"),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado")
    })
    public ResponseEntity<EntityModel<UserDTO>> update(
            @Parameter(description = "ID del usuario a actualizar", required = true, example = "1")
            @PathVariable Long id,
            @Valid @RequestBody UserDTO userDTO
    ) {
        User userEntity = this.convertToEntity(userDTO);
        User updatedUser = userService.update(id, userEntity);
        UserDTO responseDto = this.convertToDto(updatedUser);
        return ResponseEntity.ok(userModelAssembler.toModel(responseDto));
    }

    // 4. Desactivar usuario (Baja Lógica)
    @PutMapping("/{id}/desactivar")
    @Operation(summary = "Desactivar un usuario (Baja Lógica) - V2", description = "Establece el atributo estado en false")
    @ApiResponse(responseCode = "204", description = "Usuario desactivado exitosamente")
    public ResponseEntity<Void> desactivar(
            @Parameter(description = "ID del usuario a desactivar", required = true, example = "1")
            @PathVariable Long id
    ) {
        userService.desactivar(id);
        return ResponseEntity.noContent().build();
    }

    // 5. Eliminar usuario (Físico)
    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar permanentemente un usuario - V2", description = "Borra físicamente el registro de la BD")
    @ApiResponse(responseCode = "204", description = "Usuario eliminado correctamente")
    public ResponseEntity<Void> findAll(
            @Parameter(description = "ID del usuario a eliminar", required = true, example = "1")
            @PathVariable Long id
    ) {
        userService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // 6. Listar todos los usuarios
    @GetMapping
    @Operation(summary = "Listado de todos los usuarios - V2", description = "Retorna una colección HATEOAS completa")
    @ApiResponse(responseCode = "200", description = "Operación Exitosa")
    public ResponseEntity<CollectionModel<EntityModel<UserDTO>>> findAll() {
        List<EntityModel<UserDTO>> users = userService.findAll()
                .stream()
                .map(user -> userModelAssembler.toModel(this.convertToDto(user)))
                .toList();

        CollectionModel<EntityModel<UserDTO>> collectionModel = CollectionModel.of(
                users,
                linkTo(methodOn(UserControllerV2.class).findAll()).withSelfRel()
        );

        return ResponseEntity.ok(collectionModel);
    }

    // 7. Listar usuarios activos
    @GetMapping("/activos")
    @Operation(summary = "Listar usuarios activos - V2", description = "Filtra la colección devolviendo únicamente usuarios con estado activo")
    public ResponseEntity<CollectionModel<EntityModel<UserDTO>>> listarUsuariosActivos() {
        List<EntityModel<UserDTO>> activeUsers = userService.findByEstadoTrue()
                .stream()
                .map(user -> userModelAssembler.toModel(this.convertToDto(user)))
                .toList();

        CollectionModel<EntityModel<UserDTO>> collectionModel = CollectionModel.of(
                activeUsers,
                linkTo(methodOn(UserControllerV2.class).listarUsuariosActivos()).withSelfRel()
        );

        return ResponseEntity.ok(collectionModel);
    }

    // 8. Listar usuarios inactivos
    @GetMapping("/inactivos")
    @Operation(summary = "Listar usuarios inactivos - V2", description = "Filtra la colección devolviendo únicamente usuarios dados de baja")
    public ResponseEntity<CollectionModel<EntityModel<UserDTO>>> listarUsuariosInactivos() {
        List<EntityModel<UserDTO>> inactiveUsers = userService.findByEstadoFalse()
                .stream()
                .map(user -> userModelAssembler.toModel(this.convertToDto(user)))
                .toList();

        CollectionModel<EntityModel<UserDTO>> collectionModel = CollectionModel.of(
                inactiveUsers,
                linkTo(methodOn(UserControllerV2.class).listarUsuariosInactivos()).withSelfRel()
        );

        return ResponseEntity.ok(collectionModel);
    }
}