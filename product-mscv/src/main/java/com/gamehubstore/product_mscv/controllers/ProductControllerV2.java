package com.gamehubstore.product_mscv.controllers;

import com.gamehubstore.product_mscv.assemblers.ProductModelAssembler;
import com.gamehubstore.product_mscv.models.Product;
import com.gamehubstore.product_mscv.models.dtos.ProductDTO;
import com.gamehubstore.product_mscv.services.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
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
@RequestMapping("/api/v2/products")
@Validated
@Tag(name="Productos V2", description = "Métodos CRUD con soporte HATEOAS para la gestión de productos usando ProductDTO")
public class ProductControllerV2 {

    @Autowired
    private ProductModelAssembler productModelAssembler;

    @Autowired
    private ProductService productService;

    // Método utilitario privado para mapear la entidad Product a ProductDTO de forma exacta
    private ProductDTO convertToDto(Product product) {
        if (product == null) return null;
        ProductDTO dto = new ProductDTO();
        dto.setIdProduct(product.getIdProduct()); // Mapea idProducto (Entidad) a idProduct (DTO)
        dto.setNombreProduct(product.getNombreProduct());
        dto.setMarca(product.getMarca());
        dto.setModelo(product.getModelo());
        dto.setPrecio(product.getPrecio());
        dto.setIdCategory(product.getIdCategory());
        dto.setDescripcion(product.getDescripcion());
        dto.setEstado(product.getEstado());
        dto.setImagenUrl(product.getImagenUrl());

        if(product.getAudit() != null){
            dto.setAudit(product.getAudit());
        }

        return dto;
    }

    // 1. LISTAR TODOS LOS PRODUCTOS
    @GetMapping
    @Operation(
            summary = "Listado de todos los productos",
            description = "Se devuelve un CollectionModel con los ProductDTO y enlaces navegables de la API"
    )
    @ApiResponse(responseCode = "200", description = "Operación Exitosa")
    public ResponseEntity<CollectionModel<EntityModel<ProductDTO>>> findAll() {
        List<EntityModel<ProductDTO>> products = this.productService.findAll()
                .stream()
                .map(productModelAssembler::toModel)
                .toList();

        CollectionModel<EntityModel<ProductDTO>> collectionModel = CollectionModel.of(
                products,
                linkTo(methodOn(ProductControllerV2.class).findAll()).withSelfRel()
        );

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(collectionModel);
    }

    // 2. BUSCAR UN PRODUCTO POR ID
    @GetMapping("/{id}")
    @Operation(
            summary = "Búsqueda de un producto",
            description = "Se devuelve un ProductDTO enriquecido con enlaces HATEOAS, en caso contrario se lanza una excepción"
    )
    @ApiResponses(value={
            @ApiResponse(
                    responseCode = "200",
                    description = "Producto encontrado",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ProductDTO.class),
                            examples = {
                                    @ExampleObject(
                                            name = "Ejemplo Producto",
                                            value = "{\"idProduct\": 1, \"nombreProduct\": \"PlayStation 5\", \"marca\": \"Sony\", \"modelo\": \"Slim Slim\", \"precio\": 499.99, \"idCategory\": 1, \"descripcion\": \"Consola de ultima generacion\", \"estado\": true}"
                                    )
                            }
                    )),
            @ApiResponse(responseCode = "404", description = "El ID del producto buscado no existe en el sistema.")
    })
    public ResponseEntity<EntityModel<ProductDTO>> findById(
            @Parameter(description = "Id del producto a buscar", required = true, example = "1")
            @PathVariable Long id
    ) {
        Product product = this.productService.findById(id);
        ProductDTO dto = convertToDto(product);
        EntityModel<ProductDTO> entityModel = this.productModelAssembler.toModel(dto);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(entityModel);
    }

    // 3. GUARDAR UN PRODUCTO NUEVO
    @PostMapping
    @Operation(summary = "Guardado de producto", description = "Crea un producto y retorna el recurso DTO junto a sus enlaces de navegación")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Producto a crear", required = true,
            content = @Content(schema = @Schema(implementation = ProductDTO.class))
    )
    public ResponseEntity<EntityModel<ProductDTO>> save(@Valid @RequestBody Product product) {
        Product productCreate = this.productService.save(product);
        ProductDTO dto = convertToDto(productCreate);
        EntityModel<ProductDTO> entityModel = this.productModelAssembler.toModel(dto);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(entityModel);
    }

    // 4. FILTRAR PRODUCTOS POR CATEGORÍA
    @GetMapping("/category/{idCategory}")
    @Operation(
            summary = "Filtrar productos por categoría",
            description = "Se devuelve una colección de ProductDTO asociados a un ID de categoría específico"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Búsqueda realizada con éxito"),
            @ApiResponse(responseCode = "404", description = "El ID de la categoría buscada no existe en el sistema.")
    })
    public ResponseEntity<CollectionModel<EntityModel<ProductDTO>>> findByCategory(
            @Parameter(description = "ID numérico de la categoría a filtrar", required = true, example = "2")
            @PathVariable Long idCategory
    ) {
        List<EntityModel<ProductDTO>> products = this.productService.findByIdCategory(idCategory)
                .stream()
                .map(this::convertToDto)
                .map(productModelAssembler::toModel)
                .toList();

        CollectionModel<EntityModel<ProductDTO>> collectionModel = CollectionModel.of(
                products,
                linkTo(methodOn(ProductControllerV2.class).findByCategory(idCategory)).withSelfRel()
        );

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(collectionModel);
    }

    // 5. FILTRAR PRODUCTOS POR MARCA
    @GetMapping("/marca/{marca}")
    @Operation(
            summary = "Filtrar productos por marca",
            description = "Se devuelve una colección de ProductDTO filtrados por su fabricante comercial"
    )
    @ApiResponse(responseCode = "200", description = "Operación Exitosa")
    public ResponseEntity<CollectionModel<EntityModel<ProductDTO>>> findByMarca(
            @Parameter(description = "Nombre de la marca comercial", required = true, example = "ASUS")
            @PathVariable String marca
    ) {
        List<EntityModel<ProductDTO>> products = this.productService.findByMarca(marca)
                .stream()
                .map(this::convertToDto)
                .map(productModelAssembler::toModel)
                .toList();

        CollectionModel<EntityModel<ProductDTO>> collectionModel = CollectionModel.of(
                products,
                linkTo(methodOn(ProductControllerV2.class).findByMarca(marca)).withSelfRel()
        );

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(collectionModel);
    }

    // 6. FILTRAR PRODUCTOS POR ESTADO
    @GetMapping("/estado/{estado}")
    @Operation(
            summary = "Filtrar productos por estado",
            description = "Se devuelve una colección de ProductDTO activos o inactivos según el valor lógico"
    )
    @ApiResponse(responseCode = "200", description = "Operación Exitosa")
    public ResponseEntity<CollectionModel<EntityModel<ProductDTO>>> findByEstado(
            @Parameter(description = "Estado de disponibilidad (true/false)", required = true, example = "true")
            @PathVariable Boolean estado
    ) {
        List<EntityModel<ProductDTO>> products = this.productService.findByEstado(estado)
                .stream()
                .map(this::convertToDto)
                .map(productModelAssembler::toModel)
                .toList();

        CollectionModel<EntityModel<ProductDTO>> collectionModel = CollectionModel.of(
                products,
                linkTo(methodOn(ProductControllerV2.class).findByEstado(estado)).withSelfRel()
        );

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(collectionModel);
    }

    // 7. ACTUALIZAR UN PRODUCTO EXISTENTE
    @PutMapping("/{id}")
    @Operation(summary = "Actualización de producto", description = "Se actualizan los datos de un producto existente y retorna el recurso DTO con sus nuevos hipermedios")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Producto a actualizar", required = true,
            content = @Content(schema = @Schema(implementation = ProductDTO.class))
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Producto actualizado"),
            @ApiResponse(responseCode = "404", description = "No se encontró el producto con el ID especificado para ser modificado.")
    })
    public ResponseEntity<EntityModel<ProductDTO>> update(
            @Parameter(description = "Id del producto a actualizar", required = true, example = "1")
            @PathVariable Long id,
            @Valid @RequestBody Product product
    ) {
        Product productUpdate = this.productService.update(product, id);
        ProductDTO dto = convertToDto(productUpdate);
        EntityModel<ProductDTO> entityModel = this.productModelAssembler.toModel(dto);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(entityModel);
    }

    // 8. ELIMINAR PERMANENTEMENTE UN PRODUCTO
    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminación de producto", description = "Se elimina permanentemente un producto de la DB")
    @ApiResponse(responseCode = "204", description = "Producto eliminado")
    public ResponseEntity<Void> delete(
            @Parameter(description = "Id del producto a eliminar", required = true, example = "1")
            @PathVariable Long id
    ) {
        this.productService.delete(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    // 9. DESACTIVACIÓN LÓGICA DE UN PRODUCTO
    @PatchMapping("/{id}")
    @Operation(summary = "Desactivar un producto", description = "Cambia el estado del producto a inactivo (false)")
    @ApiResponse(responseCode = "204", description = "Producto desactivado con éxito")
    public ResponseEntity<Void> desactivar(
            @Parameter(description = "Id del producto a desactivar", required = true, example = "1")
            @PathVariable Long id
    ) {
        this.productService.desactivar(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}