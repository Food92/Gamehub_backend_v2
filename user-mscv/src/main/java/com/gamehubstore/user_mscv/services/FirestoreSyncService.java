package com.gamehubstore.user_mscv.services;

import com.gamehubstore.user_mscv.models.User;
import com.google.cloud.firestore.Firestore;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class FirestoreSyncService {

    private static final Logger log = LoggerFactory.getLogger(FirestoreSyncService.class);
    private static final String COLECCION_USUARIOS = "usuarios";

    @Autowired(required = false)
    private Firestore firestore;

    @PostConstruct
    public void init() {
        log.info("[Firebase] FirestoreSyncService listo en user-mscv (firestoreActivo={})", firestore != null);
    }

    public boolean isDisponible() {
        return firestore != null;
    }

    public boolean sincronizarUsuario(User user) {
        if (firestore == null) {
            log.warn("[Firebase] Firestore no disponible; se omitió sincronizar usuario id={}",
                    user != null ? user.getUserId() : null);
            return false;
        }
        if (user == null || user.getUserId() == null) {
            return false;
        }
        try {
            Map<String, Object> doc = new LinkedHashMap<>();
            doc.put("idUsuario", user.getUserId());
            doc.put("rut", user.getRut());
            doc.put("nombreCompleto", user.getNombreCompleto());
            doc.put("apellidoCompleto", user.getApellidoCompleto());
            doc.put("correo", user.getCorreo());
            doc.put("telefono", user.getTelefono());
            doc.put("estado", Boolean.TRUE.equals(user.getEstado()));
            doc.put("actualizadoEn", Instant.now().toString());

            firestore.collection(COLECCION_USUARIOS)
                    .document(String.valueOf(user.getUserId()))
                    .set(doc)
                    .get(15, TimeUnit.SECONDS);
            log.info("[Firebase] Usuario id={} sincronizado en Firestore ({})", user.getUserId(), COLECCION_USUARIOS);
            return true;
        } catch (Exception ex) {
            log.error("[Firebase] No se pudo sincronizar usuario id={} en Firestore: {}", user.getUserId(), ex.getMessage(), ex);
            return false;
        }
    }

    public void eliminarUsuario(Long userId) {
        if (firestore == null || userId == null) {
            return;
        }
        try {
            firestore.collection(COLECCION_USUARIOS)
                    .document(String.valueOf(userId))
                    .delete()
                    .get(15, TimeUnit.SECONDS);
            log.info("[Firebase] Usuario id={} eliminado en Firestore ({})", userId, COLECCION_USUARIOS);
        } catch (Exception ex) {
            log.error("[Firebase] No se pudo eliminar usuario id={} en Firestore: {}", userId, ex.getMessage(), ex);
        }
    }
}
