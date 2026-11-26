package com.gamehubstore.authentificator_mscv.controllers;

import com.gamehubstore.authentificator_mscv.dtos.AuthResponse;
import com.gamehubstore.authentificator_mscv.dtos.LoginRequest;
import com.gamehubstore.authentificator_mscv.dtos.RegisterRequest;
import com.gamehubstore.authentificator_mscv.services.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "1. Autenticación Pública", description = "Endpoints de acceso para clientes, operadores y administradores de GameHub")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    @Operation(summary = "Registrar nueva cuenta de acceso", description = "Crea las credenciales y devuelve el JWT. Roles válidos: ROLE_CLIENTE, ROLE_OPERADOR, ROLE_ADMIN")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(this.authService.register(request));
    }

    @PostMapping("/login")
    @Operation(summary = "Iniciar sesión en la tienda", description = "Valida el correo y clave, emitiendo el token JWT si la cuenta está activa")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(this.authService.login(request));
    }

    @PostMapping({"/renovar", "/refresh"})
    @Operation(summary = "Renovar token JWT", description = "Valida el token JWT vigente y emite un nuevo token firmado con expiración renovada")
    @SecurityRequirement(name = "bearer-jwt")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Token JWT renovado exitosamente"),
            @ApiResponse(responseCode = "401", description = "Token ausente, expirado o inválido"),
            @ApiResponse(responseCode = "403", description = "La cuenta se encuentra desactivada")
    })
    public ResponseEntity<AuthResponse> renovar(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Se requiere autenticación");
        }
        return ResponseEntity.ok(this.authService.renovarToken(authentication.getName()));
    }
}
