package com.gamehubstore.user_mscv.config;

import com.gamehubstore.user_mscv.repositories.UserRepository;
import com.gamehubstore.user_mscv.services.FirestoreSyncService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataLoader implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataLoader.class);

    private final UserRepository userRepository;

    @Autowired(required = false)
    private FirestoreSyncService firestoreSyncService;

    public DataLoader(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public void run(String... args) {
        var usuarios = this.userRepository.findAll();
        if (this.firestoreSyncService == null || !this.firestoreSyncService.isDisponible()) {
            log.warn("[Firebase] Firestore no está disponible en user-mscv; se omitió la sincronización inicial de {} usuarios.",
                    usuarios.size());
            return;
        }

        long sincronizados = usuarios.stream()
                .filter(this.firestoreSyncService::sincronizarUsuario)
                .count();
        log.info("[Firebase] Sincronización inicial en Firestore completada en user-mscv: {}/{} usuarios.",
                sincronizados, usuarios.size());
    }
}
