package com.gamehub_store.gateway_mscv.security;

import java.nio.charset.StandardCharsets;
import java.util.List;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverterAdapter;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

@Configuration
public class SecurityConfig {

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration cors = new CorsConfiguration();
        cors.setAllowedOrigins(List.of(
                "http://localhost:5173",
                "http://localhost:5174",
                "http://127.0.0.1:5173",
                "http://127.0.0.1:5174"
        ));
        cors.setAllowedMethods(
                List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        cors.setAllowedHeaders(List.of("Authorization", "Content-Type", "Accept", "Origin", "X-Requested-With"));
        cors.setExposedHeaders(List.of("Authorization"));
        cors.setAllowCredentials(true);
        cors.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", cors);
        return source;
    }

    @Bean
    ReactiveJwtDecoder jwtDecoder(
            @Value("${jwt.secret:ClaveSecretaSuperLargaParaGameHubStore2026!}") String secret) {
        SecretKey key = new SecretKeySpec(
                secret.getBytes(StandardCharsets.UTF_8),
                "HmacSHA256");

        return NimbusReactiveJwtDecoder
                .withSecretKey(key)
                .macAlgorithm(MacAlgorithm.HS256)
                .build();
    }

    @Bean
    SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        JwtGrantedAuthoritiesConverter authorities =
                new JwtGrantedAuthoritiesConverter();
        authorities.setAuthoritiesClaimName("roles");
        authorities.setAuthorityPrefix("");

        JwtAuthenticationConverter jwtConverter =
                new JwtAuthenticationConverter();
        jwtConverter.setJwtGrantedAuthoritiesConverter(authorities);

        ReactiveJwtAuthenticationConverterAdapter converter =
                new ReactiveJwtAuthenticationConverterAdapter(jwtConverter);

        return http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(exchange -> exchange
                        // 1. Endpoints públicos: autenticación, documentación OpenAPI y preflight CORS
                        .pathMatchers(
                                "/api/v1/auth/login",
                                "/api/v1/auth/register",
                                "/docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**",
                                "/api-docs/**",
                                "/webjars/**"
                        ).permitAll()
                        .pathMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // 2. Catálogo público (lectura de productos, categorías, stock, reseñas y cupones)
                        .pathMatchers(
                                HttpMethod.GET,
                                "/api/v1/products",
                                "/api/v1/products/**",
                                "/api/v2/products",
                                "/api/v2/products/**",
                                "/api/v1/categories",
                                "/api/v1/categories/**",
                                "/api/v2/categories",
                                "/api/v2/categories/**",
                                "/api/v1/inventories/stock",
                                "/api/v1/promotions/validar",
                                "/api/v1/promotions/active",
                                "/api/v1/reviews/product/**",
                                "/api/v1/reviews/resumen",
                                "/api/v1/shippings/seguimiento/**"
                        ).permitAll()
                        .pathMatchers(HttpMethod.POST, "/api/v1/orders/carrito/validar", "/api/v1/promotions/validar").permitAll()

                        // 3. Productos y Categorías: mutaciones exclusivas de ADMIN (VENDEDOR y CLIENTE -> 403)
                        .pathMatchers(HttpMethod.POST, "/api/v1/products/**", "/api/v2/products/**", "/api/v1/categories/**", "/api/v2/categories/**").hasRole("ADMIN")
                        .pathMatchers(HttpMethod.PUT, "/api/v1/products/**", "/api/v2/products/**", "/api/v1/categories/**", "/api/v2/categories/**").hasRole("ADMIN")
                        .pathMatchers(HttpMethod.PATCH, "/api/v1/products/**", "/api/v2/products/**", "/api/v1/categories/**", "/api/v2/categories/**").hasRole("ADMIN")
                        .pathMatchers(HttpMethod.DELETE, "/api/v1/products/**", "/api/v2/products/**", "/api/v1/categories/**", "/api/v2/categories/**").hasRole("ADMIN")

                        // 4. Órdenes:
                        // - Lectura por usuario: ADMIN, VENDEDOR, CLIENTE
                        // - Listado general y detalle: ADMIN y VENDEDOR (solo lectura para VENDEDOR)
                        // - Creación de compra: ADMIN y CLIENTE (VENDEDOR -> 403)
                        // - Edición / cambio de estado / cancelación: solo ADMIN
                        .pathMatchers(HttpMethod.GET, "/api/v1/orders/user/**").hasAnyRole("ADMIN", "VENDEDOR", "CLIENTE")
                        .pathMatchers(HttpMethod.GET, "/api/v1/orders", "/api/v1/orders/**").hasAnyRole("ADMIN", "VENDEDOR")
                        .pathMatchers(HttpMethod.POST, "/api/v1/orders").hasAnyRole("ADMIN", "CLIENTE")
                        .pathMatchers(HttpMethod.PUT, "/api/v1/orders/**").hasRole("ADMIN")
                        .pathMatchers(HttpMethod.PATCH, "/api/v1/orders/**").hasRole("ADMIN")
                        .pathMatchers(HttpMethod.DELETE, "/api/v1/orders/**").hasRole("ADMIN")

                        // 5. Perfil propio y renovación de token
                        .pathMatchers(HttpMethod.GET, "/api/v1/cuentas/me").authenticated()
                        .pathMatchers(HttpMethod.POST, "/api/v1/auth/renovar", "/api/v1/auth/refresh").authenticated()

                        // 6. Gestión de Usuarios y Cuentas:
                        // - Cliente puede consultar/registrar/actualizar su propio perfil comprador
                        // - Toda gestión administrativa de usuarios/cuentas es exclusiva de ADMIN (VENDEDOR -> 403)
                        .pathMatchers(HttpMethod.GET, "/api/v1/users/correo").hasAnyRole("ADMIN", "CLIENTE")
                        .pathMatchers(HttpMethod.PUT, "/api/v1/users/correo").hasAnyRole("ADMIN", "CLIENTE")
                        .pathMatchers(HttpMethod.POST, "/api/v1/users").hasAnyRole("ADMIN", "CLIENTE")
                        .pathMatchers("/api/v1/users", "/api/v1/users/**", "/api/v2/users/**", "/api/v1/direcciones/**", "/api/v2/addresses/**").hasRole("ADMIN")
                        .pathMatchers("/api/v1/cuentas", "/api/v1/cuentas/**").hasRole("ADMIN")

                        // 7. Pagos de cliente y administración de módulos operativos
                        .pathMatchers(HttpMethod.POST, "/api/v1/payments", "/api/v1/payments/simular").hasAnyRole("ADMIN", "CLIENTE")
                        .pathMatchers(HttpMethod.GET, "/api/v1/payments/order/**").hasAnyRole("ADMIN", "VENDEDOR", "CLIENTE")
                        .pathMatchers("/api/v1/inventories/**", "/api/v1/promotions/**", "/api/v1/payments/**", "/api/v1/shippings/**", "/api/v1/warranties/**", "/api/v1/notifications/**").hasRole("ADMIN")

                        .anyExchange().authenticated())
                .oauth2ResourceServer(oauth ->
                        oauth.jwt(jwt ->
                                jwt.jwtAuthenticationConverter(converter)))
                .build();
    }
}