package com.gamehubstore.user_mscv.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Configuration
public class FirebaseConfig {

    private static final Logger log = LoggerFactory.getLogger(FirebaseConfig.class);
    private static final String SERVICE_NAME = "user-mscv";

    @Value("${firebase.enabled:true}")
    private boolean firebaseEnabled;

    @Value("${firebase.credentials.path:../firebase-service-account.json}")
    private String credentialsPath;

    @PostConstruct
    public void logStartupConfig() {
        String cwd = Paths.get("").toAbsolutePath().normalize().toString();
        log.info("[Firebase] Config cargada en {} (enabled={}, credentialsPath={}, workingDir={})",
                SERVICE_NAME, firebaseEnabled, credentialsPath, cwd);
        System.out.println("[Firebase] Config cargada en " + SERVICE_NAME
                + " (enabled=" + firebaseEnabled + ", credentialsPath=" + credentialsPath + ", workingDir=" + cwd + ")");
    }

    @Bean
    public Firestore firestore() {
        if (!firebaseEnabled) {
            log.info("[Firebase] Firestore deshabilitado por configuración en {}.", SERVICE_NAME);
            return null;
        }
        try {
            if (FirebaseApp.getApps().isEmpty()) {
                Path ruta = resolverRutaCredenciales(credentialsPath);
                if (ruta == null) {
                    log.warn("[Firebase] No se encontró el archivo de credenciales '{}' en {} (workingDir={}); sincronización omitida.",
                            credentialsPath, SERVICE_NAME, Paths.get("").toAbsolutePath().normalize());
                    return null;
                }
                try (InputStream serviceAccount = new FileInputStream(ruta.toFile())) {
                    FirebaseOptions options = FirebaseOptions.builder()
                            .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                            .build();
                    FirebaseApp.initializeApp(options);
                    log.info("[Firebase] Firebase Firestore inicializado correctamente en {} desde {}", SERVICE_NAME, ruta);
                    System.out.println("[Firebase] Firebase Firestore inicializado correctamente en " + SERVICE_NAME + " desde " + ruta);
                }
            } else {
                log.info("[Firebase] Reutilizando instancia existente de FirebaseApp en {}.", SERVICE_NAME);
            }
            return FirestoreClient.getFirestore();
        } catch (Throwable ex) {
            log.error("[Firebase] Error al inicializar Firebase Firestore en {}: {}", SERVICE_NAME, ex.getMessage(), ex);
            System.err.println("[Firebase] Error al inicializar Firebase Firestore en " + SERVICE_NAME + ": " + ex.getMessage());
            return null;
        }
    }

    private Path resolverRutaCredenciales(String pathValue) {
        List<Path> candidatas = new ArrayList<>();
        candidatas.add(intentarRuta("firebase-service-account.json"));
        candidatas.add(intentarRuta("..", "firebase-service-account.json"));
        if (pathValue != null && !pathValue.isBlank()) {
            Path directa = intentarRuta(pathValue.trim());
            if (directa != null) {
                candidatas.add(directa);
                if (!directa.isAbsolute()) {
                    Path padre = intentarRuta("..", pathValue.trim());
                    if (padre != null) {
                        candidatas.add(padre);
                    }
                }
            }
        }

        for (Path candidata : candidatas) {
            if (candidata != null && Files.isRegularFile(candidata)) {
                return candidata.toAbsolutePath().normalize();
            }
        }
        return null;
    }

    private Path intentarRuta(String first, String... more) {
        try {
            return Paths.get(first, more);
        } catch (InvalidPathException ex) {
            log.debug("[Firebase] Ruta candidata inválida ignorada: {}", ex.getMessage());
            return null;
        }
    }
}
