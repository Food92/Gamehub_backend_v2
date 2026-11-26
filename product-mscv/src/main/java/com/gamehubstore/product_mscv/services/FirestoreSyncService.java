package com.gamehubstore.product_mscv.services;

import com.gamehubstore.product_mscv.models.Product;
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
    private static final String COLECCION_PRODUCTOS = "productos";

    @Autowired(required = false)
    private Firestore firestore;

    @PostConstruct
    public void init() {
        log.info("[Firebase] FirestoreSyncService listo en product-mscv (firestoreActivo={})", firestore != null);
    }

    public boolean isDisponible() {
        return firestore != null;
    }

    public boolean sincronizarProducto(Product product) {
        if (firestore == null) {
            log.warn("[Firebase] Firestore no disponible; se omitió sincronizar producto id={}",
                    product != null ? product.getIdProduct() : null);
            return false;
        }
        if (product == null || product.getIdProduct() == null) {
            return false;
        }
        try {
            Map<String, Object> doc = new LinkedHashMap<>();
            doc.put("idProducto", product.getIdProduct());
            doc.put("idCategoria", product.getIdCategory());
            doc.put("nombre", product.getNombreProduct());
            doc.put("marca", product.getMarca());
            doc.put("modelo", product.getModelo());
            doc.put("precio", product.getPrecio());
            doc.put("precioOferta", product.getPrecioOferta());
            doc.put("enOferta", Boolean.TRUE.equals(product.getEnOferta()));
            doc.put("descripcion", product.getDescripcion());
            doc.put("imagenUrl", product.getImagenUrl());
            doc.put("mesesGarantia", product.getMesesGarantia());
            doc.put("estado", Boolean.TRUE.equals(product.getEstado()));
            doc.put("actualizadoEn", Instant.now().toString());

            firestore.collection(COLECCION_PRODUCTOS)
                    .document(String.valueOf(product.getIdProduct()))
                    .set(doc)
                    .get(15, TimeUnit.SECONDS);
            log.info("[Firebase] Producto id={} sincronizado en Firestore ({})", product.getIdProduct(), COLECCION_PRODUCTOS);
            return true;
        } catch (Exception ex) {
            log.error("[Firebase] No se pudo sincronizar producto id={} en Firestore: {}", product.getIdProduct(), ex.getMessage(), ex);
            return false;
        }
    }

    public void eliminarProducto(Long idProduct) {
        if (firestore == null || idProduct == null) {
            return;
        }
        try {
            firestore.collection(COLECCION_PRODUCTOS)
                    .document(String.valueOf(idProduct))
                    .delete()
                    .get(15, TimeUnit.SECONDS);
            log.info("[Firebase] Producto id={} eliminado en Firestore ({})", idProduct, COLECCION_PRODUCTOS);
        } catch (Exception ex) {
            log.error("[Firebase] No se pudo eliminar producto id={} en Firestore: {}", idProduct, ex.getMessage(), ex);
        }
    }
}
