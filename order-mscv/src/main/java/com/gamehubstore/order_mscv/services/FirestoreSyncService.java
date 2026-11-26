package com.gamehubstore.order_mscv.services;

import com.gamehubstore.order_mscv.models.DetailOrder;
import com.gamehubstore.order_mscv.models.Order;
import com.google.cloud.firestore.Firestore;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class FirestoreSyncService {

    private static final Logger log = LoggerFactory.getLogger(FirestoreSyncService.class);
    private static final String COLECCION_BOLETAS = "boletas";
    private static final String COLECCION_DETALLE_BOLETA = "detalle_boleta";

    @Autowired(required = false)
    private Firestore firestore;

    @PostConstruct
    public void init() {
        log.info("[Firebase] FirestoreSyncService listo en order-mscv (firestoreActivo={})", firestore != null);
    }

    public boolean isDisponible() {
        return firestore != null;
    }

    public boolean sincronizarBoletaYDetalles(Order order) {
        if (firestore == null) {
            log.warn("[Firebase] Firestore no disponible; se omitió sincronizar boleta id={}",
                    order != null ? order.getOrderId() : null);
            return false;
        }
        if (order == null || order.getOrderId() == null) {
            return false;
        }
        try {
            String boletaDocId = String.valueOf(order.getOrderId());
            List<DetailOrder> detalles = order.getDetails() != null ? order.getDetails() : List.of();

            Map<String, Object> boletaDoc = new LinkedHashMap<>();
            boletaDoc.put("idBoleta", order.getOrderId());
            boletaDoc.put("idUsuario", order.getUserId());
            boletaDoc.put("estado", order.getEstado());
            boletaDoc.put("subtotal", order.getSubtotal());
            boletaDoc.put("descuento", order.getDescuento());
            boletaDoc.put("total", order.getTotal());
            boletaDoc.put("codigoPromocion", order.getPromotionCode());
            boletaDoc.put("fecha", order.getFecha() != null ? order.getFecha().toString() : null);
            boletaDoc.put("cantidadItems", detalles.size());
            boletaDoc.put("actualizadoEn", Instant.now().toString());

            firestore.collection(COLECCION_BOLETAS).document(boletaDocId).set(boletaDoc).get(15, TimeUnit.SECONDS);

            int linea = 1;
            for (DetailOrder detail : detalles) {
                String detalleDocId = order.getOrderId() + "_" + linea;
                Map<String, Object> detalleDoc = new LinkedHashMap<>();
                detalleDoc.put("idDetalle", detalleDocId);
                detalleDoc.put("idBoleta", order.getOrderId());
                detalleDoc.put("idProducto", detail.getIdProducto());
                detalleDoc.put("cantidad", detail.getCantidad());
                detalleDoc.put("precioUnitario", detail.getPrecioUnitario());
                detalleDoc.put("subtotalLinea", detail.getPrecioTotal());
                detalleDoc.put("actualizadoEn", Instant.now().toString());

                firestore.collection(COLECCION_DETALLE_BOLETA).document(detalleDocId).set(detalleDoc).get(15, TimeUnit.SECONDS);
                linea++;
            }

            log.info("[Firebase] Boleta id={} y {} detalles sincronizados en Firestore ({}, {})",
                    order.getOrderId(), detalles.size(), COLECCION_BOLETAS, COLECCION_DETALLE_BOLETA);
            return true;
        } catch (Exception ex) {
            log.error("[Firebase] No se pudo sincronizar boleta id={} en Firestore: {}", order.getOrderId(), ex.getMessage(), ex);
            return false;
        }
    }
}
