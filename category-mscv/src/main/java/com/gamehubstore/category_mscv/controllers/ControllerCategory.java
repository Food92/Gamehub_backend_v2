package com.gamehubstore.category_mscv.controllers;

import com.gamehubstore.category_mscv.models.Category;
import com.gamehubstore.category_mscv.models.dtos.CategoryDTO;
import com.gamehubstore.category_mscv.services.CategoryService;
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

// @RestController: cada metodo devuelve datos (JSON), no vistas HTML.
// @RequestMapping: prefijo comun de las rutas. Aqui es la version 1 de la API.
// @Validated: activa la validacion de los @Valid de los metodos.
// @Tag: agrupa estos endpoints bajo un nombre en Swagger UI.
@Tag(name = "Categorias V1", description = "Metodos CRUD para la gestión de categorías")
@RestController
@Validated
@RequestMapping("/api/v1/categories")
public class ControllerCategory {

    // @Autowired: Spring inyecta automaticamente la implementacion del servicio.
    @Autowired
    private CategoryService categoryService;

    // @GetMapping sin ruta => GET /api/v1/categories
    // @Operation y @ApiResponse son solo documentacion: describen el endpoint en Swagger.
    @GetMapping
    @Operation(
            summary = "Listado de todas las categorías",
            description = "Se devuelve una lista con las categorías que se encuentran en la tabla categorías de la DB"
    )
    @ApiResponse(responseCode = "200", description = "Operación Exitosa")
    public ResponseEntity<List<Category>> findAll() {
        // ResponseEntity.ok(...) = cuerpo + codigo HTTP 200.
        return ResponseEntity.ok(categoryService.findAll());
    }

    // {id} es una variable de ruta: GET /api/v1/categories/5 => id = 5.
    @GetMapping("/{id}")
    @Operation(
            summary = "Búsqueda de una categoría",
            description = "Se devuelve una categoría, en caso contrario se devuelve una excepcion"
    )
    // @ApiResponses documenta los posibles codigos de respuesta. @Content/@Schema
    // muestran en Swagger la forma del JSON final.
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Categoría encontrada",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CategoryDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "La categoría buscada no existe en la BD",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(type = "object", example = "{\"codigo\": \"NOT_FOUND\", \"mensaje\": \"La categoría solicitada no existe en el catálogo.\", \"detalles\": \"uri=/api/v1/categories/\"}")
                    )
            )
    })
    public ResponseEntity<Category> findById(
            // @PathVariable toma el valor de {id} de la URL. @Parameter solo lo documenta.
            @Parameter(description = "Id de la categoría a buscar", required = true, example = "1")
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(categoryService.findById(id));
    }

    // @PostMapping => POST /api/v1/categories. Se usa para crear.
    @PostMapping
    @Operation(summary = "Guardado de categoría", description = "Esta es la forma de guardar una categoría")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Categoría a crear", required = true,
            content = @Content(schema = @Schema(implementation = CategoryDTO.class))
    )
    // @Valid: valida el body contra las reglas del modelo (@NotBlank, @Pattern, etc.).
    // @RequestBody: convierte el JSON recibido en un objeto Category.
    public ResponseEntity<Category> create(@Valid @RequestBody Category category) {
        return ResponseEntity.status(HttpStatus.CREATED).body(categoryService.save(category));
    }

    // @PutMapping => PUT /api/v1/categories/{id}. Se usa para actualizar una existente.
    @PutMapping("/{id}")
    @Operation(summary = "Actualización de categoría", description = "Se actualizan los datos de una categoría existente")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Categoría a actualizar", required = true,
            content = @Content(schema = @Schema(implementation = CategoryDTO.class))
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Categoría actualizada",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CategoryDTO.class)
                    )
            ),
            @ApiResponse(responseCode = "400", description = "Datos de actualización inválidos."),
            @ApiResponse(
                    responseCode = "404",
                    description = "La categoría a modificar no se encuentra en la BD",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(type = "object", example = "{\"codigo\": \"NOT_FOUND\", \"mensaje\": \"La categoría a modificar no existe en el catálogo.\", \"detalles\": \"uri=/api/v1/categories/\"}")
                    )
            )
    })
    public ResponseEntity<Category> update(
            @Parameter(description = "Id de la categoría a actualizar", required = true, example = "1")
            @PathVariable Long id,
            @Valid @RequestBody Category category
    ) {
        Category updated = categoryService.update(id, category.getNombreCategory(), category.getDescripcionCategory());
        return ResponseEntity.ok(updated);
    }

    // @DeleteMapping => DELETE /api/v1/categories/{id}.
    @DeleteMapping("/{id}")
    @Operation(summary = "Desactivar una categoría del catálogo", description = "Aplica un borrado lógico o desactivación del registro de la categoría")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Categoría desactivada exitosamente (No Content)"),
            @ApiResponse(
                    responseCode = "404",
                    description = "La categoría que se intenta desactivar no existe en la BD",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(type = "object", example = "{\"codigo\": \"NOT_FOUND\", \"mensaje\": \"La categoría a desactivar no fue encontrada.\", \"detalles\": \"uri=/api/v1/categories/\"}")
                    )
            )
    })
    public ResponseEntity<Void> desactivar(
            @Parameter(description = "Id de la categoría a la que se le aplicará la desactivación o baja lógica", required = true, example = "1")
            @PathVariable Long id
    ) {
        categoryService.desactivar(id);
        // 204 No Content: se borro bien y no hay cuerpo que devolver.
        return ResponseEntity.noContent().build();
    }
}