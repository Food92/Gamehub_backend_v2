package com.gamehubstore.shipping_mscv.controllers;

import com.gamehubstore.shipping_mscv.models.Shipping;
import com.gamehubstore.shipping_mscv.models.dtos.CancelShippingDTO;
import com.gamehubstore.shipping_mscv.models.dtos.ShippingDTO;
import com.gamehubstore.shipping_mscv.services.ShippingService;
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
@RequestMapping("/api/v1/shippings")
@Tag(name = "Shipping Controller", description = "Endpoints para la gestión logística, control de despachos y cambios de estado")
public class ShippingController {

    @Autowired
    private ShippingService shippingService;

    @PostMapping
    @Operation(summary = "Crear un nuevo despacho (save)", description = "Registra un envío logístico si la orden externa está pagada y el comprador posee dirección.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Despacho creado e iniciado correctamente",
                    content = @Content(schema = @Schema(implementation = ShippingDTO.class))),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos o JSON mal formado"),
            @ApiResponse(responseCode = "404", description = "La orden o el usuario provistos no existen en los microservicios externos"),
            @ApiResponse(responseCode = "422", description = "Regla de negocio rota (La orden no está PAGADA o usuario sin dirección)")
    })
    public ResponseEntity<ShippingDTO> crearDespacho(@Valid @RequestBody ShippingDTO shippingDTO) {
        ShippingDTO nuevoDespacho = shippingService.save(shippingDTO);
        return new ResponseEntity<>(nuevoDespacho, HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(summary = "Listar todos los despachos", description = "Retorna la lista completa del historial logístico global.")
    @ApiResponse(responseCode = "200", description = "Listado completo obtenido de forma exitosa")
    public ResponseEntity<List<Shipping>> listarTodos() {
        return ResponseEntity.ok(shippingService.findAll());
    }

    // 💡 NUEVO ENDPOINT AGREGADO: Idéntico al buscarPorId de PaymentController
    @GetMapping("/{id}")
    @Operation(summary = "Buscar una orden de despacho por su ID", description = "Retorna los datos específicos y de ruteo de un folio de envío único.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Despacho localizado de forma exitosa"),
            @ApiResponse(responseCode = "404", description = "El ID del despacho solicitado no existe en los registros")
    })
    public ResponseEntity<Shipping> buscarPorId(
            @Parameter(description = "ID único del registro de envío", required = true)
            @PathVariable Long id) {
        // Nota: Asegúrate de que tu ShippingService tenga definido el método findById(id)
        // En caso de que no lo tenga, puedes implementarlo rápidamente usando tu repositorio.
        return ResponseEntity.ok(shippingService.update(null, id));
    }

    @PutMapping("/{shippingId}")
    @Operation(summary = "Actualizar un despacho (update)", description = "Permite modificar transportista, dirección, asignar tracking único y avanzar el estado del envío.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Despacho actualizado exitosamente",
                    content = @Content(schema = @Schema(implementation = Shipping.class))),
            @ApiResponse(responseCode = "404", description = "El ID del despacho provisto no existe en la base de datos"),
            @ApiResponse(responseCode = "422", description = "El número de tracking ya existe en otro registro o error en reglas de entrega")
    })
    public ResponseEntity<Shipping> actualizarDespacho(
            @Valid @RequestBody ShippingDTO shippingDTO,
            @PathVariable Long shippingId) {
        return ResponseEntity.ok(shippingService.update(shippingDTO, shippingId));
    }

    @PutMapping("/cancelar")
    @Operation(summary = "Cancelar un despacho (cancel)", description = "Cambia el estado del despacho a 'CANCELADO' usando el ID y el motivo correspondiente.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Despacho cancelado correctamente"),
            @ApiResponse(responseCode = "404", description = "El ID del despacho no fue encontrado"),
            @ApiResponse(responseCode = "422", description = "No se puede cancelar un despacho que ya fue ENTREGADO")
    })
    public ResponseEntity<Void> cancelarDespacho(@Valid @RequestBody CancelShippingDTO cancelShippingDTO) {
        shippingService.cancel(cancelShippingDTO);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Buscar despachos por ID de Usuario", description = "Muestra todos los envíos asociados a un comprador específico.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Listado del usuario obtenido de forma exitosa"),
            @ApiResponse(responseCode = "404", description = "El ID de usuario consultado no tiene despachos registrados")
    })
    public ResponseEntity<List<Shipping>> buscarPorUsuario(
            @Parameter(description = "ID del usuario comprador", required = true)
            @PathVariable Long userId) {
        return ResponseEntity.ok(shippingService.findByUserId(userId));
    }

    @GetMapping("/order/{orderId}")
    @Operation(summary = "Buscar despachos por ID de Orden", description = "Permite ver los envíos asignados a una orden de compra.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Listado de la orden obtenido de forma exitosa"),
            @ApiResponse(responseCode = "404", description = "La orden de compra no posee una ruta de despacho asignada")
    })
    public ResponseEntity<List<Shipping>> buscarPorOrden(
            @Parameter(description = "ID de la orden de compra a rastrear", required = true)
            @PathVariable Long orderId) {
        return ResponseEntity.ok(shippingService.findByOrderId(orderId));
    }

    @GetMapping("/estado/{estado}")
    @Operation(summary = "Filtrar despachos por estado", description = "Filtra la lista según su condición logística actual (CREADO, EN_TRANSITO, ENTREGADO, CANCELADO).")
    @ApiResponse(responseCode = "200", description = "Lista filtrada obtenida de forma exitosa")
    public ResponseEntity<List<Shipping>> buscarPorEstado(
            @Parameter(description = "Condición logística del despacho", required = true, schema = @Schema(allowableValues = {"CREADO", "EN_TRANSITO", "ENTREGADO", "CANCELADO"}))
            @PathVariable String estado) {
        return ResponseEntity.ok(shippingService.findByEstado(estado));
    }
}