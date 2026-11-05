package com.gamehubstore.authentificator_mscv.config;

import com.gamehubstore.authentificator_mscv.models.CuentaAcceso;
import com.gamehubstore.authentificator_mscv.repositories.CuentaAccesoRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class DataLoader implements CommandLineRunner {

    private final CuentaAccesoRepository cuentaRepository;
    private final PasswordEncoder passwordEncoder;

    public DataLoader(CuentaAccesoRepository cuentaRepository, PasswordEncoder passwordEncoder) {
        this.cuentaRepository = cuentaRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        crearCuentaSiNoExiste("admin@gamehub.com", "admin123", "ROLE_ADMIN");
        crearCuentaSiNoExiste("operador@gamehub.com", "op123", "ROLE_OPERADOR");
        crearCuentaSiNoExiste("cliente@gmail.com", "cliente123", "ROLE_CLIENTE");
    }

    private void crearCuentaSiNoExiste(String email, String passwordPlano, String rol) {
        if (this.cuentaRepository.existsByEmail(email)) {
            return;
        }
        CuentaAcceso cuenta = new CuentaAcceso();
        cuenta.setEmail(email);
        cuenta.setPasswordHash(this.passwordEncoder.encode(passwordPlano));
        cuenta.setRol(rol);
        cuenta.setEstado(true);
        cuenta.setFechaCreacion(LocalDateTime.now());

        this.cuentaRepository.save(cuenta);
    }
}
