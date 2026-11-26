package com.gamehubstore.inventory_mscv.config;

import com.gamehubstore.inventory_mscv.models.Inventory;
import com.gamehubstore.inventory_mscv.repositories.InventoryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataLoader implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataLoader.class);

    private final InventoryRepository inventoryRepository;

    public DataLoader(InventoryRepository inventoryRepository) {
        this.inventoryRepository = inventoryRepository;
    }

    @Override
    public void run(String... args) {
        if (this.inventoryRepository.count() > 0) {
            return;
        }

        crearStockSiNoExiste(1L, 15L, 3L, "Bodega Central - Pasillo A1");
        crearStockSiNoExiste(2L, 25L, 5L, "Bodega Central - Pasillo B2");
        crearStockSiNoExiste(3L, 12L, 3L, "Bodega Central - Pasillo C1");
        crearStockSiNoExiste(4L, 8L, 2L, "Bodega Central - Pasillo D3");
        crearStockSiNoExiste(5L, 10L, 2L, "Bodega Central - Pasillo D4");
        crearStockSiNoExiste(6L, 6L, 2L, "Bodega Central - Pasillo E1");

        log.info("Stock inicial cargado en inventory-mscv ({} registros).", this.inventoryRepository.count());
    }

    private void crearStockSiNoExiste(Long idProducto, Long stockDisponible, Long stockMinimo, String ubicacion) {
        if (!this.inventoryRepository.findByIdProducto(idProducto).isEmpty()) {
            return;
        }
        Inventory inventory = new Inventory();
        inventory.setIdProducto(idProducto);
        inventory.setStockDisponible(stockDisponible);
        inventory.setStockReservado(0L);
        inventory.setStockMinimo(stockMinimo);
        inventory.setUbicacion(ubicacion);
        this.inventoryRepository.save(inventory);
    }
}
