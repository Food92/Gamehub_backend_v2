package com.gamehubstore.category_mscv.config;

import com.gamehubstore.category_mscv.models.Category;
import com.gamehubstore.category_mscv.repositories.CategoryRepository;
import com.gamehubstore.category_mscv.services.FirestoreSyncService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataLoader implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataLoader.class);

    private final CategoryRepository categoryRepository;

    @Autowired(required = false)
    private FirestoreSyncService firestoreSyncService;

    public DataLoader(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    public void run(String... args) {
        if (this.categoryRepository.count() == 0) {
            crearSiNoExiste("Consolas", "Consolas de videojuegos de última generación y accesorios oficiales.");
            crearSiNoExiste("Periféricos", "Teclados mecánicos, mouse gamer, headsets y accesorios de precisión.");
            crearSiNoExiste("Monitores", "Monitores gaming de alta tasa de refresco y baja latencia.");
            crearSiNoExiste("Hardware", "Tarjetas gráficas, procesadores y notebooks de alto rendimiento.");
            log.info("Categorías iniciales cargadas en category-mscv ({} registros).", this.categoryRepository.count());
        }

        var categorias = this.categoryRepository.findAll();
        if (this.firestoreSyncService == null || !this.firestoreSyncService.isDisponible()) {
            log.warn("[Firebase] Firestore no está disponible en category-mscv; se omitió la sincronización inicial de {} categorías.",
                    categorias.size());
            return;
        }

        long sincronizadas = categorias.stream()
                .filter(this.firestoreSyncService::sincronizarCategoria)
                .count();
        log.info("[Firebase] Sincronización inicial en Firestore completada en category-mscv: {}/{} categorías.",
                sincronizadas, categorias.size());
    }

    private void crearSiNoExiste(String nombre, String descripcion) {
        if (this.categoryRepository.existsByNombreCategory(nombre)) {
            return;
        }
        Category category = new Category();
        category.setNombreCategory(nombre);
        category.setDescripcionCategory(descripcion);
        category.setEstado(Category.EstadoCategory.ACTIVO);
        this.categoryRepository.save(category);
    }
}
