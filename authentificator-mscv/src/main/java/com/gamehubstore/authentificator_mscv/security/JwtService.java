package com.gamehubstore.authentificator_mscv.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gamehubstore.authentificator_mscv.models.CuentaAcceso;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class JwtService {
    private static final Logger log = LoggerFactory.getLogger(JwtService.class);

    private final JwtEncoder jwtEncoder;

    // Se inyecta el JwtEncoder que ya definiste en tu SecurityConfig
    public JwtService(JwtEncoder jwtEncoder) {
        this.jwtEncoder = jwtEncoder;
    }

    public String generarToken(CuentaAcceso cuenta) {
        Instant ahora = Instant.now();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("gamehub-auth-service")
                .issuedAt(ahora)
                .expiresAt(ahora.plusSeconds(3600)) // Expira en 1 hora
                .subject(cuenta.getEmail())
                .claim("userId", cuenta.getId())
                .claim("roles", cuenta.getRol())
                .build();

        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        return this.jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }
}