package com.gamehubstore.authentificator_mscv.controllers;

import com.gamehubstore.authentificator_mscv.dtos.AuthResponse;
import com.gamehubstore.authentificator_mscv.dtos.LoginRequest;
import com.gamehubstore.authentificator_mscv.dtos.RegisterRequest;
import com.gamehubstore.authentificator_mscv.services.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
}
