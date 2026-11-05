package com.gamehubstore.order_mscv.controllers;

import com.gamehubstore.order_mscv.models.Order;
import com.gamehubstore.order_mscv.models.dtos.OrderDTO;
import com.gamehubstore.order_mscv.services.OrderService;
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
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/orders")
@Tag(name = "Order Controller", description = "Endpoints públicos para la creación, consulta y gestión del ciclo de vida de las órdenes de compra")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @PostMapping
    @Operation(summary = "Crear una nueva orden de compra", description = "Recibe los productos de un carrito, se comunica con el Inventario para reservar stock y genera la orden en estado 'CREADA'.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Orden creada exitosamente",
                    content = @Content(schema = @Schema(implementation = OrderDTO.class))),
            @ApiResponse(responseCode = "400", description = "Cuerpo de la petición inválido o mal estructurado"),
            @ApiResponse(responseCode = "422", description = "Error de negocio (Falta de stock o producto inexistente)")
    })
    public ResponseEntity<OrderDTO> crearOrden(@Valid @RequestBody OrderDTO orderDTO) {
        OrderDTO nuevaOrden = orderService.save(orderDTO);
        return new ResponseEntity<>(nuevaOrden, HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(summary = "Listar todas las órdenes", description = "Retorna el historial completo de órdenes registradas en el sistema.")
    @ApiResponse(responseCode = "200", description = "Lista de órdenes obtenida correctamente")
    public ResponseEntity<List<Order>> listarTodas() {
        return ResponseEntity.ok(orderService.findAll());
    }

    @GetMapping("/{orderId}")
    @Operation(summary = "Buscar orden por su ID", description = "Retorna una orden específica con todos sus detalles en formato puro de entidad.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Orden encontrada"),
            @ApiResponse(responseCode = "404", description = "La orden solicitada no existe")
    })
    public ResponseEntity<Order> buscarPorId(@PathVariable Long orderId) {
        return ResponseEntity.ok(orderService.findById(orderId));
    }

    @GetMapping("/{orderId}/dto")
    @Operation(summary = "Buscar orden por su ID en formato DTO", description = "Retorna la estructura optimizada de una orden (útil para respuestas ligeras de cara al cliente).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Orden en formato DTO encontrada"),
            @ApiResponse(responseCode = "404", description = "La orden solicitada no existe")
    })
    public ResponseEntity<OrderDTO> buscarPorIdDTO(@PathVariable Long orderId) {
        return ResponseEntity.ok(orderService.findByIdDTO(orderId));
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Listar órdenes de un usuario", description = "Retorna todas las compras realizadas por un cliente específico.")
    @ApiResponse(responseCode = "200", description = "Lista de órdenes del cliente")
    public ResponseEntity<List<Order>> buscarPorUsuario(@PathVariable Long userId) {
        return ResponseEntity.ok(orderService.findByUserId(userId));
    }

    @GetMapping("/estado/{estado}")
    @Operation(summary = "Filtrar órdenes por estado", description = "Permite buscar las órdenes según su condición en el flujo (Ej: CREADA, PAGADA, CANCELADA, EN_DESPACHO).")
    @ApiResponse(responseCode = "200", description = "Resultados filtrados exitosamente")
    public ResponseEntity<List<Order>> buscarPorEstado(@PathVariable String estado) {
        return ResponseEntity.ok(orderService.findByEstado(estado.toUpperCase()));
    }

    @PutMapping("/{orderId}")
    @Operation(summary = "Actualizar datos generales de una orden", description = "Permite modificar datos básicos de una orden únicamente si se encuentra todavía en estado 'CREADA'.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Orden editada correctamente"),
            @ApiResponse(responseCode = "422", description = "La orden no se puede modificar porque ya cambió de estado")
    })
    public ResponseEntity<Order> actualizarOrden(@Valid @RequestBody OrderDTO orderDTO, @PathVariable Long orderId) {
        return ResponseEntity.ok(orderService.update(orderDTO, orderId));
    }

    // ENDPOINT PARA EL VENDEDOR/SISTEMA: Cambiar el estado del pedido (ej: pasar de PAGADA a EN_DESPACHO)
    @PatchMapping("/{orderId}/estado")
    @Operation(
            summary = "Actualizar estado de una orden (Webhook/Vendedor)",
            description = "Avanza el ciclo de vida de una orden. Si cambia a 'PAGADA', notifica internamente para procesar las rebajas físicas definitivas del Inventario."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Estado de la orden actualizado exitosamente",
                    content = @Content(schema = @Schema(implementation = OrderDTO.class))),
            @ApiResponse(responseCode = "400", description = "El parámetro 'estado' se envió vacío o nulo"),
            @ApiResponse(responseCode = "404", description = "La orden con el ID provisto no existe"),
            @ApiResponse(responseCode = "422", description = "Transición de estado inválida por reglas de negocio")
    })
    public ResponseEntity<OrderDTO> cambiarEstado(
            @Parameter(description = "ID de la orden de compra a modificar", required = true, example = "1")
            @PathVariable Long orderId,

            @Parameter(description = "Nuevo estado de la orden", required = true, schema = @Schema(allowableValues = {"CREADA", "PAGADA", "EN_DESPACHO", "ENTREGADA", "CANCELADA"}))
            @RequestParam String estado) {

        if (estado == null || estado.trim().isEmpty()) {
            throw new IllegalArgumentException("El parámetro 'estado' no puede estar vacío.");
        }

        OrderDTO ordenActualizada = orderService.updateEstado(orderId, estado.toUpperCase().trim());
        return ResponseEntity.ok(ordenActualizada);
    }

    @DeleteMapping("/{orderId}/cancelar")
    @Operation(summary = "Cancelar una orden de compra", description = "Cancela la orden de compra y, en caso de estar en estado válido, devuelve las unidades retenidas al inventario.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Orden cancelada con éxito"),
            @ApiResponse(responseCode = "422", description = "No se puede cancelar una orden que ya fue despachada o entregada")
    })
    public ResponseEntity<Order> cancelarOrden(@PathVariable Long orderId) {
        return ResponseEntity.ok(orderService.cancelOrder(orderId));
    }
}