package com.gamehubstore.authentificator_mscv.controllers;

import com.gamehubstore.authentificator_mscv.dtos.ActivoRequest;
import com.gamehubstore.authentificator_mscv.models.CuentaAcceso;
import com.gamehubstore.authentificator_mscv.dtos.RegisterRequest;
import com.gamehubstore.authentificator_mscv.services.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/cuentas")
@Tag(name = "2. Administración de Cuentas (CRUD)")
@SecurityRequirement(name = "bearer-jwt")
public class CuentaAccesoController {

    private final AuthService authService;

    public CuentaAccesoController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping
    @Operation(summary = "Listar todas las cuentas de acceso", description = "Retorna el listado completo de cuentas registradas (solo ADMIN)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<CuentaAcceso>> findAll() {
        return ResponseEntity.ok(this.authService.listarCuentas());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar cuenta por ID", description = "Obtiene una cuenta de acceso por su identificador (solo ADMIN)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CuentaAcceso> findById(@PathVariable Long id) {
        return ResponseEntity.ok(this.authService.buscarPorId(id));
    }

    @GetMapping("/buscar")
    @Operation(summary = "Buscar cuenta por correo electrónico", description = "Obtiene una cuenta por su email (solo ADMIN)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CuentaAcceso> findByEmail(@RequestParam String email) {
        return ResponseEntity.ok(this.authService.buscarPorEmail(email));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar rol, clave o estado de una cuenta", description = "Modifica datos de una cuenta existente (solo ADMIN)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CuentaAcceso> update(@PathVariable Long id, @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(this.authService.actualizarCuenta(id, request));
    }

    @PutMapping("/{id}/desactivar")
    @Operation(summary = "Desactivar cuenta por ID", description = "Realiza baja lógica de una cuenta de acceso (solo ADMIN)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deactivate(@PathVariable Long id) {
        this.authService.desactivarCuenta(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/activo")
    @Operation(summary = "Activar o desactivar una cuenta")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CuentaAcceso> cambiarActivo(
            @PathVariable Long id,
            @Valid @RequestBody ActivoRequest request,
            Authentication authentication) {
        String adminEmail = authentication != null ? authentication.getName() : null;
        return ResponseEntity.ok(this.authService.cambiarActivo(id, request.getActivo(), adminEmail));
    }




    // Este método NO recibe un @PathVariable (como id) por seguridad.
    // En su lugar, lee directamente el token JWT de la cabecera para saber
    // con total certeza quién está realizando la petición.
    @GetMapping("/me")
    @Operation(summary = "Obtener el perfil del usuario autenticado actualmente")
    public ResponseEntity<CuentaAcceso> obtenerMiPerfil(Authentication authentication) {
        // Extrae el email (sub) del Token JWT actual
        String emailUsuarioAutenticado = authentication.getName();

        // Retorna solo la cuenta de quien está logueado
        CuentaAcceso miCuenta = this.authService.buscarPorEmail(emailUsuarioAutenticado);

        return ResponseEntity.ok(miCuenta);
    }

}