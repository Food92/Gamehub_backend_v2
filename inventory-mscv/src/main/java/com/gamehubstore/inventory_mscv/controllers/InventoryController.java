package com.gamehubstore.inventory_mscv.controllers;

import com.gamehubstore.inventory_mscv.models.Inventory;
import com.gamehubstore.inventory_mscv.models.InventoryReservation;
import com.gamehubstore.inventory_mscv.models.dtos.InventoryDTO;
import com.gamehubstore.inventory_mscv.services.InventoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Inventario V1", description = "Endpoints para el control de existencias, reservas de stock y auditoría de bodega")
@RestController
@RequestMapping("/api/v1/inventories")
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @GetMapping
    @Operation(summary = "Listar todo el inventario", description = "Obtiene una lista completa con los registros de existencias de todos los productos")
    public ResponseEntity<List<Inventory>> getAllInventories() {
        return ResponseEntity.ok(inventoryService.findAll());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar stock por ID de registro", description = "Retorna el estado de inventario de un registro específico en base a su ID numérico")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Registro de inventario encontrado"),
            @ApiResponse(
                    responseCode = "404",
                    description = "El ID de inventario solicitado no existe",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(type = "object", example = "{\"codigo\": \"NOT_FOUND\", \"mensaje\": \"El registro de inventario solicitado no existe.\", \"detalles\": \"uri=/api/v1/inventories/\"}")
                    )
            )
    })
    public ResponseEntity<Inventory> getInventoryById(
            @Parameter(description = "ID único del registro de inventario", required = true, example = "1")
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(inventoryService.findById(id));
    }

    @PostMapping
    @Operation(summary = "Crear inventario inicial", description = "Registra por primera vez el stock y ubicación de un producto, validando mediante Feign que el producto exista y esté activo")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Inventario inicial creado exitosamente e historial de ENTRADA registrado"),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos o el producto se encuentra inactivo/inexistente")
    })
    public ResponseEntity<InventoryDTO> createInventory(@Valid @RequestBody InventoryDTO inventoryDTO) {
        return new ResponseEntity<>(inventoryService.save(inventoryDTO), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar parámetros de inventario", description = "Permite modificar manualmente la ubicación física, stock disponible o reservado de un registro")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Inventario actualizado correctamente"),
            @ApiResponse(
                    responseCode = "404",
                    description = "El ID de inventario especificado para modificar no existe",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(type = "object", example = "{\"codigo\": \"NOT_FOUND\", \"mensaje\": \"El registro de inventario a modificar no existe.\", \"detalles\": \"uri=/api/v1/inventories/\"}")
                    )
            )
    })
    public ResponseEntity<Inventory> updateInventory(
            @Parameter(description = "ID del registro de inventario a modificar", required = true, example = "1")
            @PathVariable Long id,
            @RequestBody Inventory inventory
    ) {
        return ResponseEntity.ok(inventoryService.update(id, inventory));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar un registro de inventario", description = "Remueve físicamente el control de stock de la base de datos mediante su ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Registro eliminado con éxito (No Content)"),
            @ApiResponse(
                    responseCode = "404",
                    description = "El ID de inventario solicitado para eliminación no existe",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(type = "object", example = "{\"codigo\": \"NOT_FOUND\", \"mensaje\": \"El registro de inventario a eliminar no fue encontrado.\", \"detalles\": \"uri=/api/v1/inventories/\"}")
                    )
            )
    })
    public ResponseEntity<Void> deleteInventory(
            @Parameter(description = "ID del registro a eliminar", required = true, example = "1")
            @PathVariable Long id
    ) {
        inventoryService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/product/{idProducto}")
    @Operation(summary = "Buscar stock por ID de Producto", description = "Retorna las existencias asociadas directamente al identificador de un producto del catálogo")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de stock asociada al producto obtenida correctamente"),
            @ApiResponse(
                    responseCode = "404",
                    description = "No se encontraron registros de inventario configurados para el ID de producto provisto",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(type = "object", example = "{\"codigo\": \"NOT_FOUND\", \"mensaje\": \"No se encontró una configuración de inventario para el producto especificado.\", \"detalles\": \"uri=/api/v1/inventories/product/\"}")
                    )
            )
    })
    public ResponseEntity<List<Inventory>> getInventoryByProducto(
            @Parameter(description = "ID numérico del producto en el catálogo", required = true, example = "10")
            @PathVariable Long idProducto
    ) {
        return ResponseEntity.ok(inventoryService.findByIdProducto(idProducto));
    }

    @GetMapping("/location")
    @Operation(summary = "Buscar stock por ubicación", description = "Filtra los registros de inventario que pertenezcan a una zona o pasillo específico de la bodega")
    public ResponseEntity<List<Inventory>> getInventoryByUbicacion(
            @Parameter(description = "Nombre de la ubicación física en bodega", required = true, example = "Pasillo A - Estante 4")
            @RequestParam String ubicacion
    ) {
        return ResponseEntity.ok(inventoryService.findByUbicacion(ubicacion));
    }

    @PutMapping("/reservations/{orderId}/{idProducto}")
    public ResponseEntity<InventoryReservation> reservarParaOrden(@PathVariable Long orderId, @PathVariable Long idProducto, @RequestParam Long cantidad) {
        return ResponseEntity.ok(inventoryService.reservarParaOrden(orderId, idProducto, cantidad));
    }

    @PutMapping("/reservations/{orderId}/{idProducto}/confirm")
    public ResponseEntity<InventoryReservation> confirmarReserva(@PathVariable Long orderId, @PathVariable Long idProducto) {
        return ResponseEntity.ok(inventoryService.confirmarReserva(orderId, idProducto));
    }

    @DeleteMapping("/reservations/{orderId}/{idProducto}")
    public ResponseEntity<InventoryReservation> liberarReserva(@PathVariable Long orderId, @PathVariable Long idProducto) {
        return ResponseEntity.ok(inventoryService.liberarReserva(orderId, idProducto));
    }

    @PutMapping("/reserve/{idProducto}")
    @Operation(summary = "Reservar stock (Flujo 3 - Ordenes)", description = "Pasa stock del pozo 'disponible' al pozo 'reservado' de forma temporal mientras se procesa una compra. Si el stock disponible es insuficiente, arroja excepción.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Reserva exitosa y movimiento 'RESERVA' insertado en el historial"),
            @ApiResponse(responseCode = "400", description = "Stock disponible insuficiente en bodega para cubrir la solicitud"),
            @ApiResponse(
                    responseCode = "404",
                    description = "No se encontró un registro de control de inventario base para el producto solicitado",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(type = "object", example = "{\"codigo\": \"NOT_FOUND\", \"mensaje\": \"No se puede reservar stock porque no existe el registro base de este producto.\", \"detalles\": \"uri=/api/v1/inventories/reserve/\"}")
                    )
            )
    })
    public ResponseEntity<Inventory> reservarStock(
            @Parameter(description = "ID del producto a reservar", required = true, example = "10")
            @PathVariable Long idProducto,
            @Parameter(description = "Cantidad de unidades a congelar", required = true, example = "2")
            @RequestParam Long cantidad
    ) {
        return ResponseEntity.ok(inventoryService.reservarStock(idProducto, cantidad));
    }

    @PutMapping("/outbound/{idProducto}")
    @Operation(summary = "Confirmar salida definitiva (Flujo 6 - Pagos)", description = "Descuenta de forma definitiva las unidades del pozo 'reservado' una vez que la pasarela confirma el pago exitoso de la orden.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Despacho autorizado de bodega y movimiento 'SALIDA' insertado"),
            @ApiResponse(responseCode = "400", description = "Inconsistencia: No hay suficientes unidades previamente reservadas para este producto"),
            @ApiResponse(
                    responseCode = "404",
                    description = "No se encontró el registro de inventario base para procesar la salida de este producto",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(type = "object", example = "{\"codigo\": \"NOT_FOUND\", \"mensaje\": \"No existe registro de inventario base para efectuar la salida del producto.\", \"detalles\": \"uri=/api/v1/inventories/outbound/\"}")
                    )
            )
    })
    public ResponseEntity<Inventory> salidaStock(
            @Parameter(description = "ID del producto que sale de bodega", required = true, example = "10")
            @PathVariable Long idProducto,
            @Parameter(description = "Cantidad de unidades vendidas", required = true, example = "2")
            @RequestParam Long cantidad
    ) {
        return ResponseEntity.ok(inventoryService.salidaStock(idProducto, cantidad));
    }
}