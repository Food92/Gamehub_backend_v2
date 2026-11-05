package com.gamehubstore.product_mscv.controllers;

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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// @RestController: cada metodo devuelve datos (JSON), no vistas HTML.
// @RequestMapping: prefijo comun de las rutas. Aqui es la version 1 de la API.
// @Validated: activa la validacion de los @Valid de los metodos.
// @Tag: agrupa estos endpoints bajo un nombre en Swagger UI.
@RestController
@RequestMapping("/api/v1/products")
@Validated
@Tag(name="Productos V1", description = "Metodos CRUD para la gestión de productos")
public class ProductController {

    // @Autowired: Spring inyecta automaticamente la implementacion del servicio.
    @Autowired
    private ProductService productService;

    // @GetMapping sin ruta => GET /api/v1/products
    // @Operation y @ApiResponse son solo documentacion: describen el endpoint en Swagger.
    @GetMapping
    @Operation(
            summary = "Listado de todos los productos",
            description = "Se devuelve una lista con los productos que se encuentran en la tabla productos de la DB"
    )
    @ApiResponse(responseCode = "200", description = "Operacion Exitosa")
    public ResponseEntity<List<ProductDTO>> findAll() {
        // ResponseEntity.ok(...) = cuerpo + codigo HTTP 200.
        return ResponseEntity.ok(this.productService.findAll());
    }

    // {id} es una variable de ruta: GET /api/v1/products/5 => id = 5.
    @GetMapping("/{id}")
    @Operation(
            summary = "Busqueda de un producto",
            description = "Se devuelve un producto, en caso contrario se devuelve una excepcion"
    )
    // @ApiResponses documenta los posibles codigos de respuesta. @Content/@Schema/@ExampleObject
    // muestran en Swagger la forma del JSON y un ejemplo concreto.
    @ApiResponses(value={
            @ApiResponse(
                    responseCode = "200",
                    description = "Producto encontrado",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ProductDTO.class),
                            examples = {
                                    @ExampleObject(
                                            name = "Ejemplo Producto",
                                            value = "{\"nombreProduct\": \"PlayStation 5\", \"marca\": \"Sony\", \"modelo\": \"Slim Slim\", \"precio\": 499.99, \"idCategory\": 1, \"descripcion\": \"Consola de ultima generacion\", \"estado\": true}"
                                    )
                            }
                    )),
            @ApiResponse(
                    responseCode = "404",
                    description = "El ID del producto buscado no existe en el sistema.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(type = "object", example = "{\"codigo\": \"NOT_FOUND\", \"mensaje\": \"El producto solicitado no existe en el sistema.\", \"detalles\": \"uri=/api/v1/products/\"}")
                    )
            )
    })
    public ResponseEntity<Product> findById(
            // @PathVariable toma el valor de {id} de la URL. @Parameter solo lo documenta.
            @Parameter(description = "Id del producto a buscar", required = true, example = "1")
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(this.productService.findById(id));
    }

    // @PostMapping => POST /api/v1/products. Se usa para crear.
    @PostMapping
    @Operation(summary = "Guardado de producto", description = "Esta es la forma de guardar un producto")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Producto a crear", required = true,
            content = @Content(schema = @Schema(implementation = ProductDTO.class))
    )
    // @Valid: valida el body contra las reglas del modelo (@NotBlank, @NotNull, etc.).
    // @RequestBody: convierte el JSON recibido en un objeto Product.
    public ResponseEntity<Product> save(@Valid @RequestBody Product product) {
        return ResponseEntity.status(HttpStatus.CREATED).body(this.productService.save(product));
    }

    // Busqueda por categoria => GET /api/v1/products/category/2
    @GetMapping("/category/{idCategory}")
    @Operation(
            summary = "Filtrar productos por categoria",
            description = "Se devuelve una lista de productos asociados a un ID de categoria especifico"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Busqueda realizada con exito"),
            @ApiResponse(
                    responseCode = "404",
                    description = "El ID de la categoría buscada no existe en el sistema.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(type = "object", example = "{\"codigo\": \"NOT_FOUND\", \"mensaje\": \"La categoría solicitada o sus productos no existen en el sistema.\", \"detalles\": \"uri=/api/v1/products/category/\"}")
                    )
            )
    })
    public ResponseEntity<List<Product>> findByCategory(
            @Parameter(description = "ID numerico de la categoria a filtrar", required = true, example = "2")
            @PathVariable Long idCategory
    ) {
        return ResponseEntity.ok(this.productService.findByIdCategory(idCategory));
    }

    // Busqueda por marca => GET /api/v1/products/marca/ASUS
    @GetMapping("/marca/{marca}")
    @Operation(
            summary = "Filtrar productos por marca",
            description = "Se devuelve una lista de productos filtrados por su fabricante comercial"
    )
    @ApiResponse(responseCode = "200", description = "Operacion Exitosa")
    public ResponseEntity<List<Product>> findByMarca(
            @Parameter(description = "Nombre de la marca comercial", required = true, example = "ASUS")
            @PathVariable String marca
    ) {
        return ResponseEntity.ok(this.productService.findByMarca(marca));
    }

    // Busqueda por estado => GET /api/v1/products/estado/true
    @GetMapping("/estado/{estado}")
    @Operation(
            summary = "Filtrar productos por estado",
            description = "Se devuelve una lista de productos activos o inactivos segun el valor logico"
    )
    @ApiResponse(responseCode = "200", description = "Operacion Exitosa")
    public ResponseEntity<List<Product>> findByEstado(
            @Parameter(description = "Estado de disponibilidad (true/false)", required = true, example = "true")
            @PathVariable Boolean estado
    ) {
        return ResponseEntity.ok(this.productService.findByEstado(estado));
    }

    // @PutMapping => PUT /api/v1/products/{id}. Se usa para actualizar uno existente.
    @PutMapping("/{id}")
    @Operation(summary = "Actualizacion de producto", description = "Se actualizan los datos de un producto existente")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Producto a actualizar", required = true,
            content = @Content(schema = @Schema(implementation = ProductDTO.class))
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Producto actualizado"),
            @ApiResponse(
                    responseCode = "404",
                    description = "No se encontró el producto con el ID especificado para ser modificado.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(type = "object", example = "{\"codigo\": \"NOT_FOUND\", \"mensaje\": \"El producto a modificar no existe en el sistema.\", \"detalles\": \"uri=/api/v1/products/\"}")
                    )
            )
    })
    public ResponseEntity<Product> update(
            @Parameter(description = "Id del producto a actualizar", required = true, example = "1")
            @PathVariable Long id,
            @Valid @RequestBody Product product
    ) {
        return ResponseEntity.ok(this.productService.update(product, id));
    }

    // @DeleteMapping => DELETE /api/v1/products/{id}.
    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminacion de producto", description = "Se elimina permanentemente un producto de la DB")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Producto eliminado"),
            @ApiResponse(
                    responseCode = "404",
                    description = "El producto que se intenta eliminar no existe en el sistema.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(type = "object", example = "{\"codigo\": \"NOT_FOUND\", \"mensaje\": \"El producto a eliminar no fue encontrado.\", \"detalles\": \"uri=/api/v1/products/\"}")
                    )
            )
    })
    public ResponseEntity<Void> delete(
            @Parameter(description = "Id del producto a eliminar", required = true, example = "1")
            @PathVariable Long id
    ) {
        this.productService.delete(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    // @PatchMapping => PATCH /api/v1/products/{id}. Se usa para desactivacion logica.
    @PatchMapping("/{id}")
    @Operation(summary = "Desactivar un producto", description = "Cambia el estado del producto a inactivo (false)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Producto desactivado con exito"),
            @ApiResponse(
                    responseCode = "404",
                    description = "El producto que se intenta desactivar no fue encontrado.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(type = "object", example = "{\"codigo\": \"NOT_FOUND\", \"mensaje\": \"El producto a desactivar no existe.\", \"detalles\": \"uri=/api/v1/products/\"}")
                    )
            )
    })
    public ResponseEntity<Void> desactivar(
            @Parameter(description = "Id del producto a desactivar", required = true, example = "1")
            @PathVariable Long id
    ) {
        this.productService.desactivar(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}