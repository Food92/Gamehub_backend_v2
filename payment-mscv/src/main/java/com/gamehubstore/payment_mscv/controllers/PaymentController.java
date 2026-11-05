package com.gamehubstore.payment_mscv.controllers;

import com.gamehubstore.payment_mscv.models.Payment;
import com.gamehubstore.payment_mscv.models.dtos.PaymentDTO;
import com.gamehubstore.payment_mscv.services.PaymentService;
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
@RequestMapping("/api/v1/payments")
@Tag(name = "Payment Controller", description = "Endpoints para el procesamiento de transacciones financieras y control de cajas de pago")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @PostMapping
    @Operation(summary = "Procesar un nuevo pago", description = "Recibe el intento de pago, valida montos y existencia con el servicio de órdenes por Feign, y guarda el registro si todo es correcto.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Pago aprobado y registrado exitosamente",
                    content = @Content(schema = @Schema(implementation = PaymentDTO.class))),
            @ApiResponse(responseCode = "400", description = "El cuerpo JSON del pago tiene datos inválidos"),
            @ApiResponse(responseCode = "422", description = "Regla de negocio rota (Monto no coincide o la orden ya está pagada)")
    })
    public ResponseEntity<PaymentDTO> registrarPago(@Valid @RequestBody PaymentDTO paymentDTO) {
        PaymentDTO nuevoPago = paymentService.save(paymentDTO);
        return new ResponseEntity<>(nuevoPago, HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(summary = "Listar todas las transacciones", description = "Muestra una lista global con todos los pagos registrados en la base de datos.")
    @ApiResponse(responseCode = "200", description = "Historial transaccional obtenido")
    public ResponseEntity<List<Payment>> listarTodos() {
        return ResponseEntity.ok(paymentService.findAll());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar una transacción por su ID", description = "Retorna los datos específicos de un folio de pago.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Transacción localizada"),
            @ApiResponse(responseCode = "404", description = "El ID del pago solicitado no existe") // 👈 Agregado/Corregido
    })
    public ResponseEntity<Payment> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(paymentService.findById(id));
    }

    @GetMapping("/order/{orderId}")
    @Operation(summary = "Buscar pagos asociados a una Orden", description = "Permite ver el historial de intentos de pago que ha tenido una misma orden de compra.")
    @ApiResponse(responseCode = "200", description = "Lista de transacciones de la orden")
    public ResponseEntity<List<Payment>> buscarPorOrden(@PathVariable Long orderId) {
        return ResponseEntity.ok(paymentService.findByOrder(orderId));
    }

    @GetMapping("/estado/{estadoPago}")
    @Operation(summary = "Filtrar transacciones por estado financiero", description = "Filtra la lista de transacciones según su condición (Ej: APROBADO, RECHAZADO, PENDIENTE).")
    @ApiResponse(responseCode = "200", description = "Lista filtrada correctamente")
    public ResponseEntity<List<Payment>> buscarPorEstado(@PathVariable String estadoPago) {
        return ResponseEntity.ok(paymentService.findByEstadoPago(estadoPago.toUpperCase()));
    }

    @GetMapping("/tipo/{tipoPago}")
    @Operation(summary = "Filtrar por método de pago", description = "Muestra los pagos agrupados por el medio utilizado (Ej: TARJETA, PAYPAL, EFECTIVO).")
    @ApiResponse(responseCode = "200", description = "Lista filtrada correctamente")
    public ResponseEntity<List<Payment>> buscarPorTipo(@PathVariable String tipoPago) {
        return ResponseEntity.ok(paymentService.findByTipoPago(tipoPago.toUpperCase()));
    }

    @PutMapping("/{paymentId}/estado")
    @Operation(summary = "Actualizar estado de un pago (Webhook Pasarela)", description = "Permite cambiar manualmente o por respuesta bancaria el estado de una transacción y notifica en cadena a las órdenes.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Estado financiero actualizado"),
            @ApiResponse(responseCode = "404", description = "El ID del pago especificado no existe") // 👈 Mensaje descriptivo
    })
    public ResponseEntity<Payment> actualizarEstado(
            @PathVariable Long paymentId,
            @Parameter(description = "Nuevo estado de la transacción", required = true, schema = @Schema(allowableValues = {"APROBADO", "RECHAZADO", "PENDIENTE"}))
            @RequestParam String estadoPago) {
        return ResponseEntity.ok(paymentService.updatePayment(estadoPago.toUpperCase().trim(), paymentId));
    }

    @DeleteMapping("/{paymentId}/cancelar")
    @Operation(summary = "Anular o reembolsar un pago", description = "Cambia el estado de la transacción local a 'CANCELADO' y gatilla un evento para que el servicio de órdenes devuelva el stock a la tienda.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Pago anulado y reembolso notificado"),
            @ApiResponse(responseCode = "404", description = "El ID del pago no fue encontrado") // 👈 Corregido el 444 que tenías por 404
    })
    public ResponseEntity<Payment> cancelarPago(@PathVariable Long paymentId) {
        return ResponseEntity.ok(paymentService.cancelPayment(paymentId));
    }
}