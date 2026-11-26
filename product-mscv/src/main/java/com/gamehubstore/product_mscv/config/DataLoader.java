package com.gamehubstore.product_mscv.config;

import com.gamehubstore.product_mscv.models.Product;
import com.gamehubstore.product_mscv.repositories.ProductRepository;
import com.gamehubstore.product_mscv.services.FirestoreSyncService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataLoader implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataLoader.class);

    private final ProductRepository productRepository;

    @Autowired(required = false)
    private FirestoreSyncService firestoreSyncService;

    public DataLoader(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    public void run(String... args) {
        if (this.productRepository.count() == 0) {
            crearSiNoExiste(
                    "PlayStation 5 Slim",
                    "Sony",
                    "CFI-2000 1TB",
                    549990.0,
                    1L,
                    "Consola PlayStation 5 edición Slim con SSD de 1TB y control inalámbrico DualSense.",
                    "/img/ps5.jpg",
                    false,
                    null
            );

            crearSiNoExiste(
                    "Teclado Mecánico RGB Pro",
                    "HyperX",
                    "Alloy Origins 65",
                    89990.0,
                    2L,
                    "Teclado mecánico compacto 65% con switches táctiles, chasis de aluminio e iluminación RGB.",
                    "/img/teclado.jpg",
                    true,
                    74990.0
            );

            crearSiNoExiste(
                    "Monitor Gamer QHD 165Hz",
                    "ASUS",
                    "TUF VG27AQ",
                    279990.0,
                    3L,
                    "Monitor IPS de 27 pulgadas resolución 2560x1440, 165Hz, 1ms y compatibilidad G-Sync.",
                    "/img/monitor.jpg",
                    true,
                    249990.0
            );

            crearSiNoExiste(
                    "Tarjeta Gráfica RTX 4070",
                    "NVIDIA",
                    "GeForce RTX 4070 12GB",
                    699990.0,
                    4L,
                    "GPU arquitectura Ada Lovelace con 12GB GDDR6X, Ray Tracing y DLSS 3.",
                    "/img/rtx4070.jpg",
                    false,
                    null
            );

            crearSiNoExiste(
                    "Procesador Ryzen 7 7800X3D",
                    "AMD",
                    "7800X3D AM5",
                    429990.0,
                    4L,
                    "Procesador de 8 núcleos y 16 hilos con tecnología 3D V-Cache optimizado para gaming.",
                    "/img/ryzen.jpg",
                    true,
                    399990.0
            );

            crearSiNoExiste(
                    "Notebook Gamer RTX 4060",
                    "Lenovo",
                    "Legion Slim 5",
                    1199990.0,
                    4L,
                    "Notebook gamer 16 pulgadas 165Hz, 16GB DDR5, SSD 1TB y gráficos NVIDIA RTX 4060.",
                    "/img/notebook.jpg.webp",
                    false,
                    null
            );

            log.info("Productos iniciales cargados en product-mscv ({} registros).", this.productRepository.count());
        }

        var productos = this.productRepository.findAll();
        if (this.firestoreSyncService == null || !this.firestoreSyncService.isDisponible()) {
            log.warn("[Firebase] Firestore no está disponible en product-mscv; se omitió la sincronización inicial de {} productos.",
                    productos.size());
            return;
        }

        long sincronizados = productos.stream()
                .filter(this.firestoreSyncService::sincronizarProducto)
                .count();
        log.info("[Firebase] Sincronización inicial en Firestore completada en product-mscv: {}/{} productos.",
                sincronizados, productos.size());
    }

    private void crearSiNoExiste(
            String nombre,
            String marca,
            String modelo,
            Double precio,
            Long idCategory,
            String descripcion,
            String imagenUrl,
            boolean enOferta,
            Double precioOferta
    ) {
        if (this.productRepository.existsByNombreProduct(nombre)) {
            return;
        }
        Product product = new Product();
        product.setNombreProduct(nombre);
        product.setMarca(marca);
        product.setModelo(modelo);
        product.setPrecio(precio);
        product.setIdCategory(idCategory);
        product.setDescripcion(descripcion);
        product.setMesesGarantia(12);
        product.setEstado(true);
        product.setImagenUrl(imagenUrl);
        product.setEnOferta(enOferta);
        product.setPrecioOferta(precioOferta);
        this.productRepository.save(product);
    }
}
