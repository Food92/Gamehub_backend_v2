package com.gamehubstore.category_mscv.controllers;

import com.gamehubstore.category_mscv.assemblers.CategoryModelAssembler;
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
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/api/v2/categories")
@Validated
@Tag(name = "Categorías V2", description = "Endpoints CRUD optimizados con HATEOAS y transferencia vía CategoryDTO")
public class ControllerCategoryV2 {

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private CategoryModelAssembler categoryModelAssembler;

    // Conversor hacia tu DTO estricto
    private CategoryDTO convertToDto(Category category) {
        if (category == null) return null;
        CategoryDTO dto = new CategoryDTO();

        // SOLUCIÓN: Agregamos la línea para que el DTO transporte el ID
        dto.setIdCategory(category.getIdCategory());

        dto.setNombreCategory(category.getNombreCategory());
        dto.setDescripcionCategory(category.getDescripcionCategory());
        return dto;
    }

    @GetMapping
    @Operation(summary = "Listado de todas las categorías - V2")
    @ApiResponse(responseCode = "200", description = "Operación Exitosa")
    public ResponseEntity<CollectionModel<EntityModel<CategoryDTO>>> findAll() {
        List<EntityModel<CategoryDTO>> categories = this.categoryService.findAll()
                .stream()
                .map(cat -> {
                    CategoryDTO dto = this.convertToDto(cat);
                    return categoryModelAssembler.toModel(cat, dto);
                })
                .toList();

        CollectionModel<EntityModel<CategoryDTO>> collectionModel = CollectionModel.of(
                categories,
                linkTo(methodOn(ControllerCategoryV2.class).findAll()).withSelfRel()
        );

        return ResponseEntity.ok(collectionModel);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Búsqueda de una categoría por ID - V2")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Categoría encontrada"),
            @ApiResponse(responseCode = "404", description = "La categoría buscada no existe")
    })
    public ResponseEntity<EntityModel<CategoryDTO>> findById(
            @Parameter(description = "Id de la categoría a buscar", required = true, example = "1")
            @PathVariable Long id
    ) {
        Category category = this.categoryService.findById(id);
        CategoryDTO dto = this.convertToDto(category);
        EntityModel<CategoryDTO> entityModel = this.categoryModelAssembler.toModel(category, dto);

        return ResponseEntity.ok(entityModel);
    }

    @PostMapping
    @Operation(summary = "Crear categoría - V2", description = "Registra una categoría forzando su estado inicial a ACTIVO")
    public ResponseEntity<EntityModel<CategoryDTO>> create(@Valid @RequestBody Category category) {

        // CORRECCIÓN: Ajustado al Enum exacto "ACTIVO" en masculino
        if (category.getEstado() == null) {
            category.setEstado(Category.EstadoCategory.ACTIVO);
        }

        Category categoryCreated = this.categoryService.save(category);
        CategoryDTO dto = this.convertToDto(categoryCreated);
        EntityModel<CategoryDTO> entityModel = this.categoryModelAssembler.toModel(categoryCreated, dto);

        return ResponseEntity.status(HttpStatus.CREATED).body(entityModel);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar nombre o descripción - V2")
    public ResponseEntity<EntityModel<CategoryDTO>> update(
            @Parameter(description = "Id de la categoría a actualizar", required = true, example = "1")
            @PathVariable Long id,
            @Valid @RequestBody Category category
    ) {
        Category categoryUpdated = this.categoryService.update(id, category.getNombreCategory(), category.getDescripcionCategory());
        CategoryDTO dto = this.convertToDto(categoryUpdated);
        EntityModel<CategoryDTO> entityModel = this.categoryModelAssembler.toModel(categoryUpdated, dto);

        return ResponseEntity.ok(entityModel);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Desactivar categoría - V2", description = "Baja lógica respetando controles de productos activos")
    @ApiResponse(responseCode = "204", description = "Categoría desactivada exitosamente")
    public ResponseEntity<Void> desactivar(
            @Parameter(description = "Id de la categoría a desactivar", required = true, example = "1")
            @PathVariable Long id
    ) {
        this.categoryService.desactivar(id);
        return ResponseEntity.noContent().build();
    }
}