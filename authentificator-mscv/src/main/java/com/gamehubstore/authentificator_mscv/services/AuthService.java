package com.gamehubstore.authentificator_mscv.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gamehubstore.authentificator_mscv.models.CuentaAcceso;
import com.gamehubstore.authentificator_mscv.dtos.AuthResponse;
import com.gamehubstore.authentificator_mscv.dtos.LoginRequest;
import com.gamehubstore.authentificator_mscv.dtos.RegisterRequest;
import com.gamehubstore.authentificator_mscv.repositories.CuentaAccesoRepository;
// IMPORT CRÍTICO DE CONEXIÓN DE PAQUETES:
import com.gamehubstore.authentificator_mscv.security.JwtService;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AuthService {
    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final CuentaAccesoRepository cuentaRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(CuentaAccesoRepository cuentaRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService) {
        this.cuentaRepository = cuentaRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }


    @Transactional
    public AuthResponse register(RegisterRequest request) {
        log.info("operation={} started", "register");
        // 1. Validar que el correo electrónico no exista previamente
        if (this.cuentaRepository.existsByEmail(request.getEmail())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El correo electrónico ya está registrado");
        }

        // 2. Instanciar la nueva Cuenta de Acceso
        CuentaAcceso cuenta = new CuentaAcceso();
        cuenta.setEmail(request.getEmail());

        // 3. Cifrar la contraseña con BCrypt
        cuenta.setPasswordHash(this.passwordEncoder.encode(request.getPassword()));

        // 4. Asignar rol por defecto si no se envía uno específico
        String rolAsignado = (request.getRol() == null || request.getRol().isBlank())
                ? "ROLE_CLIENTE"
                : request.getRol();
        cuenta.setRol(rolAsignado);

        // 5. CONFIGURACIÓN DEL ESTADO EN REGISTER:
        // Siempre se fuerza a true al registrarse para que la cuenta quede inmediatamente operativa.
        cuenta.setEstado(true);

        cuenta.setFechaCreacion(LocalDateTime.now());

        // 6. Guardar en la Base de Datos y emitir el Token JWT inicial
        this.cuentaRepository.save(cuenta);
        return construirRespuestaToken(cuenta);
    }


    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        log.info("operation={} started", "login");
        CuentaAcceso cuenta = this.cuentaRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciales inválidas"));

        if (!cuenta.getEstado()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "La cuenta se encuentra desactivada.");
        }

        if (!this.passwordEncoder.matches(request.getPassword(), cuenta.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciales inválidas");
        }

        return construirRespuestaToken(cuenta);
    }


    @Transactional(readOnly = true)
    public List<CuentaAcceso> listarCuentas() { return this.cuentaRepository.findAll(); }

    @Transactional(readOnly = true)
    public CuentaAcceso buscarPorId(Long id) {
        return this.cuentaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cuenta no encontrada"));
    }


    @Transactional(readOnly = true)
    public CuentaAcceso buscarPorEmail(String email) {
        return this.cuentaRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cuenta no encontrada"));
    }


    @Transactional
    public CuentaAcceso actualizarCuenta(Long id, RegisterRequest request) {
        CuentaAcceso cuenta = buscarPorId(id);
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            cuenta.setPasswordHash(this.passwordEncoder.encode(request.getPassword()));
        }
        if (request.getRol() != null && !request.getRol().isBlank()) {
            cuenta.setRol(request.getRol());
        }
        if (request.getEstado() != null) {
            cuenta.setEstado(request.getEstado());
        }
        return this.cuentaRepository.save(cuenta);
    }


    @Transactional
    public void desactivarCuenta(Long id) {
        CuentaAcceso cuenta = buscarPorId(id);
        cuenta.setEstado(false);
        this.cuentaRepository.save(cuenta);
    }

    private AuthResponse construirRespuestaToken(CuentaAcceso cuenta) {
        String token = this.jwtService.generarToken(cuenta);
        return new AuthResponse(token, "Bearer", cuenta.getId(), cuenta.getEmail(), cuenta.getRol());
    }
}