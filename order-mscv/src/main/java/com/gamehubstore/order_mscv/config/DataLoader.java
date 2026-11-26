package com.gamehubstore.order_mscv.config;

import com.gamehubstore.order_mscv.repositories.OrderRepository;
import com.gamehubstore.order_mscv.services.FirestoreSyncService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataLoader implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataLoader.class);

    private final OrderRepository orderRepository;

    @Autowired(required = false)
    private FirestoreSyncService firestoreSyncService;

    public DataLoader(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Override
    public void run(String... args) {
        var ordenes = this.orderRepository.findAll();
        if (this.firestoreSyncService == null || !this.firestoreSyncService.isDisponible()) {
            log.warn("[Firebase] Firestore no está disponible en order-mscv; se omitió la sincronización inicial de {} boletas.",
                    ordenes.size());
            return;
        }

        long sincronizadas = ordenes.stream()
                .filter(this.firestoreSyncService::sincronizarBoletaYDetalles)
                .count();
        log.info("[Firebase] Sincronización inicial en Firestore completada en order-mscv: {}/{} boletas.",
                sincronizadas, ordenes.size());
    }
}
