package com.gamehubstore.warranty_mscv.controllers;

import com.gamehubstore.warranty_mscv.models.Warranty;
import com.gamehubstore.warranty_mscv.models.dtos.ErrorDetails;
import com.gamehubstore.warranty_mscv.models.dtos.WarrantyDTO;
import com.gamehubstore.warranty_mscv.services.WarrantyService;
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
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/warranties")
@Tag(name = "Warranty Controller", description = "Endpoints homologados para la gestión de solicitudes de postventa, soporte técnico y garantías")
public class WarrantyController {

    @Autowired
    private WarrantyService warrantyService;

    @PostMapping
    @Operation(summary = "Crear solicitud de garantía (createWarranty)", description = "Registra un ticket de postventa verificando la existencia del usuario, producto y orden mediante llamadas síncronas a microservicios externos.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Ticket de garantía creado exitosamente",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = WarrantyDTO.class))),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos o JSON mal formado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetails.class))),
            @ApiResponse(responseCode = "404", description = "El ID de orden, producto o usuario solicitado no existe en los ecosistemas externos",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetails.class),
                            examples = @ExampleObject(
                                    name = "Recurso No Encontrado",
                                    value = "{\n  \"codigo\": \"NOT_FOUND\",\n  \"mensaje\": \"No se pudo mapear el DTO porque la orden o producto específico no existe.\",\n  \"detalles\": \"El microservicio externo retornó un estado vacío para el ID provisto.\",\n  \"timestamp\": \"2026-06-22T19:30:00\"\n}"
                            )
                    )),
            @ApiResponse(responseCode = "422", description = "Regla de negocio rota (Fuera de plazo o la orden no está pagada/entregada)",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetails.class)))
    })
    public ResponseEntity<WarrantyDTO> crearGarantia(@Valid @RequestBody WarrantyDTO warrantyDTO) {
        WarrantyDTO nuevaGarantia = warrantyService.createWarranty(warrantyDTO);
        return new ResponseEntity<>(nuevaGarantia, HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(summary = "Listar todas las garantías (findAll)", description = "Retorna el historial completo de solicitudes de soporte global.")
    @ApiResponse(responseCode = "200", description = "Listado completo obtenido de forma exitosa",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = Warranty.class)))
    public ResponseEntity<List<Warranty>> listarTodas() {
        return ResponseEntity.ok(warrantyService.findAll());
    }



    @GetMapping("/{id}")
    @Operation(summary = "Buscar garantía por ID (findById)", description = "Recupera los detalles y estado de una solicitud de garantía específica por su ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Garantía localizada de forma exitosa",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Warranty.class))),
            @ApiResponse(responseCode = "404", description = "El ID de garantía solicitado no existe en los registros",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetails.class),
                            examples = @ExampleObject(
                                    name = "Garantía Inexistente",
                                    value = "{\n  \"codigo\": \"NOT_FOUND\",\n  \"mensaje\": \"La garantía con el ID provisto no existe.\",\n  \"detalles\": \"No se encontraron registros en la base de datos local.\",\n  \"timestamp\": \"2026-06-22T19:30:00\"\n}"
                            )
                    ))
    })
    public ResponseEntity<Warranty> buscarPorId(
            @Parameter(description = "ID único de la garantía", required = true)
            @PathVariable Long id) {
        return ResponseEntity.ok(warrantyService.findById(id));
    }

    @GetMapping("/cliente/{usuarioId}")
    @Operation(summary = "Listar garantías por Cliente (findByCliente)", description = "Muestra todas las solicitudes de soporte técnico abiertas por un usuario específico.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Listado del cliente obtenido con éxito",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Warranty.class))),
            @ApiResponse(responseCode = "404", description = "El ID de usuario consultado no tiene garantías registradas",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetails.class),
                            examples = @ExampleObject(
                                    name = "Cliente Sin Historial",
                                    value = "{\n  \"codigo\": \"NOT_FOUND\",\n  \"mensaje\": \"El cliente no registra solicitudes de postventa.\",\n  \"detalles\": \"No se encontraron tickets asociados al usuarioId provisto.\",\n  \"timestamp\": \"2026-06-22T19:30:00\"\n}"
                            )
                    ))
    })
    public ResponseEntity<List<Warranty>> buscarPorCliente(
            @Parameter(description = "ID del usuario comprador", required = true)
            @PathVariable Long usuarioId) {
        List<Warranty> garantias = warrantyService.findByCliente(usuarioId);
        return ResponseEntity.ok(garantias);
    }

    @GetMapping("/producto/{productId}")
    @Operation(summary = "Listar garantías por Producto (findByProducto)", description = "Muestra el historial de reclamos o fallas asociados a un ID del catálogo de productos.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Listado del producto obtenido con éxito",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Warranty.class))),
            @ApiResponse(responseCode = "404", description = "El ID de producto consultado no registra solicitudes de soporte técnico",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetails.class),
                            examples = @ExampleObject(
                                    name = "Producto sin Reclamos",
                                    value = "{\n  \"codigo\": \"NOT_FOUND\",\n  \"mensaje\": \"El artículo consultado no cuenta con registros de soporte técnico.\",\n  \"detalles\": \"No existen tickets de garantía vinculados a este productId.\",\n  \"timestamp\": \"2026-06-22T19:30:00\"\n}"
                            )
                    ))
    })
    public ResponseEntity<List<Warranty>> buscarPorProducto(
            @Parameter(description = "ID del producto a consultar", required = true)
            @PathVariable Long productId) {
        return ResponseEntity.ok(warrantyService.findByProducto(productId));
    }

    @GetMapping("/estado/{estado}")
    @Operation(summary = "Listar garantías por Estado (findByEstado)", description = "Filtra la lista de solicitudes según su situación logística actual (CREADO, ABIERTA, CERRADA).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista filtrada obtenida con éxito",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Warranty.class))),
            @ApiResponse(responseCode = "400", description = "Estado de consulta inválido",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetails.class)))
    })
    public ResponseEntity<List<Warranty>> buscarPorEstado(
            @Parameter(description = "Estado actual del ticket", required = true, schema = @Schema(allowableValues = {"CREADO", "ABIERTA", "CERRADA"}))
            @PathVariable String estado) {
        return ResponseEntity.ok(warrantyService.findByEstado(estado));
    }

    @PutMapping("/{id}/diagnostico")
    @Operation(summary = "Actualizar estado, diagnóstico o resolución (updateStatusOrDiagnostic)", description = "Permite a los técnicos de postventa ingresar el avance físico de la revisión del equipo.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Garantía actualizada correctamente",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Warranty.class))),
            @ApiResponse(responseCode = "404", description = "El ID de la garantía no fue encontrado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetails.class))),
            @ApiResponse(responseCode = "422", description = "No se puede modificar una garantía que ya se encuentra en estado CERRADA",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetails.class)))
    })
    public ResponseEntity<Warranty> actualizarDiagnostico(
            @PathVariable Long id,
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) String diagnostico,
            @RequestParam(required = false) String resolucion) {
        return ResponseEntity.ok(warrantyService.updateStatusOrDiagnostic(id, estado, diagnostico, resolucion));
    }

    @PutMapping("/{id}/cerrar")
    @Operation(summary = "Cerrar solicitud de garantía (closeWarranty)", description = "Finaliza el flujo de soporte técnico del artículo. Es una condición obligatoria ingresar la resolución final.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Garantía cerrada de forma exitosa",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Warranty.class))),
            @ApiResponse(responseCode = "404", description = "El ID de la garantía no existe",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetails.class))),
            @ApiResponse(responseCode = "422", description = "REGLA INCUMPLIDA: Intento de cierre sin especificar una resolución",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetails.class),
                            examples = @ExampleObject(
                                    name = "Resolución Faltante",
                                    value = "{\n  \"codigo\": \"UNPROCESSABLE_ENTITY\",\n  \"mensaje\": \"No se puede cerrar la garantía sin especificar una resolución.\",\n  \"detalles\": \"El campo resolucion es mandatorio para transicionar al estado CERRADA.\",\n  \"timestamp\": \"2026-06-22T19:30:00\"\n}"
                            )
                    ))
    })
    public ResponseEntity<Warranty> cerrarGarantia(
            @PathVariable Long id,
            @Parameter(description = "Resolución del ticket (Ej: Reemplazo de consola por falla de fábrica)", required = true)
            @RequestParam String resolucion) {
        return ResponseEntity.ok(warrantyService.closeWarranty(id, resolucion));
    }
}