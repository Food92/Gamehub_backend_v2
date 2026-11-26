package com.gamehubstore.category_mscv.services;

import com.gamehubstore.category_mscv.models.Category;
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
    private static final String COLECCION_CATEGORIAS = "categorias";

    @Autowired(required = false)
    private Firestore firestore;

    @PostConstruct
    public void init() {
        log.info("[Firebase] FirestoreSyncService listo en category-mscv (firestoreActivo={})", firestore != null);
    }

    public boolean isDisponible() {
        return firestore != null;
    }

    public boolean sincronizarCategoria(Category category) {
        if (firestore == null) {
            log.warn("[Firebase] Firestore no disponible; se omitió sincronizar categoría id={}",
                    category != null ? category.getIdCategory() : null);
            return false;
        }
        if (category == null || category.getIdCategory() == null) {
            return false;
        }
        try {
            Map<String, Object> doc = new LinkedHashMap<>();
            doc.put("idCategoria", category.getIdCategory());
            doc.put("nombre", category.getNombreCategory());
            doc.put("descripcion", category.getDescripcionCategory());
            doc.put("estado", category.getEstado() != null ? category.getEstado().name() : "ACTIVO");
            doc.put("actualizadoEn", Instant.now().toString());

            firestore.collection(COLECCION_CATEGORIAS)
                    .document(String.valueOf(category.getIdCategory()))
                    .set(doc)
                    .get(15, TimeUnit.SECONDS);
            log.info("[Firebase] Categoría id={} sincronizada en Firestore ({})", category.getIdCategory(), COLECCION_CATEGORIAS);
            return true;
        } catch (Exception ex) {
            log.error("[Firebase] No se pudo sincronizar categoría id={} en Firestore: {}", category.getIdCategory(), ex.getMessage(), ex);
            return false;
        }
    }
}
