package com.gamehubstore.inventory_mscv.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gamehubstore.inventory_mscv.client.ProductClient;
import com.gamehubstore.inventory_mscv.exceptions.InventoryException;
import com.gamehubstore.inventory_mscv.models.Inventory;
import com.gamehubstore.inventory_mscv.models.InventoryReservation;
import com.gamehubstore.inventory_mscv.repositories.InventoryReservationRepository;
import com.gamehubstore.inventory_mscv.models.MovimientoInventario;
import com.gamehubstore.inventory_mscv.models.dtos.InventoryDTO;
import com.gamehubstore.inventory_mscv.models.dtos.ProductDTO;
import com.gamehubstore.inventory_mscv.repositories.InventoryRepository;
import com.gamehubstore.inventory_mscv.repositories.MovimientoInventarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class InventoryServiceImpl implements InventoryService {
    private static final Logger log = LoggerFactory.getLogger(InventoryServiceImpl.class);

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private MovimientoInventarioRepository movimientoInventarioRepository;

    @Autowired
    private InventoryReservationRepository reservationRepository;

    @Autowired
    private ProductClient productClient;

    @Override
    @Transactional(readOnly = true)
    public List<Inventory> findAll() {
        return inventoryRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Inventory findById(Long id) {
        return inventoryRepository.findById(id).orElseThrow(
                () -> new InventoryException("Registro de inventario con ID: " + id + " no existe."));
    }

    @Override
    @Transactional
    public InventoryDTO save(InventoryDTO inventoryDTO) {
        log.info("operation={} started", "save");
        ProductDTO productDTO = productClient.findById(inventoryDTO.getIdProducto());

        if (productDTO == null || "INACTIVO".equals(productDTO.getEstado().toString())) {
            throw new InventoryException("No existe el producto o se encuentra inactivo en el catálogo");
        }

        if (inventoryDTO.getStockDisponible() < 0) {
            throw new InventoryException("El stock inicial disponible no puede ser inferior a cero");
        }

        Inventory inventory = new Inventory();
        inventory.setIdProducto(inventoryDTO.getIdProducto());
        inventory.setStockDisponible(inventoryDTO.getStockDisponible());
        inventory.setStockReservado(inventoryDTO.getStockReservado() != null ? inventoryDTO.getStockReservado() : 0L);
        inventory.setStockMinimo(inventoryDTO.getStockMinimo() != null ? inventoryDTO.getStockMinimo() : 0L);
        inventory.setUbicacion(inventoryDTO.getUbicacion());

        Inventory saved = inventoryRepository.save(inventory);

        MovimientoInventario mov = new MovimientoInventario();
        mov.setIdProducto(saved.getIdProducto());
        mov.setTipo("ENTRADA");
        mov.setCantidad(saved.getStockDisponible());
        mov.setFecha(LocalDateTime.now());
        movimientoInventarioRepository.save(mov);

        InventoryDTO response = new InventoryDTO();
        response.setInventarioId(saved.getInventarioId());
        response.setIdProducto(saved.getIdProducto());
        response.setStockDisponible(saved.getStockDisponible());
        response.setStockReservado(saved.getStockReservado());
        response.setStockMinimo(saved.getStockMinimo());
        response.setUbicacion(saved.getUbicacion());
        return response;
    }

    @Override
    @Transactional
    public Inventory update(Long id, Inventory inventory) {
        log.info("operation={} started", "update");
        Inventory existente = this.findById(id);

        if (inventory.getStockDisponible() < 0 || inventory.getStockReservado() < 0) {
            throw new InventoryException("El stock remanente no puede quedar en valores negativos");
        }

        existente.setUbicacion(inventory.getUbicacion());
        existente.setStockDisponible(inventory.getStockDisponible());
        existente.setStockReservado(inventory.getStockReservado());

        if (inventory.getStockMinimo() != null) {
            existente.setStockMinimo(inventory.getStockMinimo());
        }

        return inventoryRepository.save(existente);
    }

    @Override
    @Transactional
    public Inventory reservarStock(Long idProducto, Long cantidad) {
        log.info("operation={} started", "reservarStock");
        Inventory inventory = inventoryRepository.findByIdProducto(idProducto)
                .stream().findFirst().orElseThrow(
                        () -> new InventoryException("No se encontró un registro de inventario configurado para el producto: " + idProducto));

        if (cantidad <= 0) {
            throw new InventoryException("La cantidad especificada a reservar debe ser un valor positivo mayor a cero");
        }

        if (inventory.getStockDisponible() < cantidad) {
            throw new InventoryException("Disponibilidad insuficiente en bodega para efectuar la reserva requerida");
        }

        inventory.setStockDisponible(inventory.getStockDisponible() - cantidad);
        inventory.setStockReservado(inventory.getStockReservado() + cantidad);

        MovimientoInventario mov = new MovimientoInventario();
        mov.setIdProducto(inventory.getIdProducto());
        mov.setTipo("RESERVA");
        mov.setCantidad(cantidad);
        mov.setFecha(LocalDateTime.now());
        movimientoInventarioRepository.save(mov);

        return inventoryRepository.save(inventory);
    }

    @Override
    @Transactional
    public InventoryReservation reservarParaOrden(Long orderId, Long idProducto, Long cantidad) {
        if (orderId == null || idProducto == null || cantidad == null || cantidad <= 0) {
            throw new InventoryException("Orden, producto y cantidad positiva son obligatorios");
        }
        var existing = reservationRepository.findByOrderIdAndProductId(orderId, idProducto);
        if (existing.isPresent()) {
            InventoryReservation reservation = existing.get();
            if (!reservation.getQuantity().equals(cantidad)) throw new InventoryException("La clave de idempotencia ya se usó con otra cantidad");
            if ("LIBERADA".equals(reservation.getStatus())) throw new InventoryException("La reserva fue liberada y no puede reabrirse para esta orden");
            return reservation;
        }
        Inventory inventory = inventoryRepository.findByIdProductoForUpdate(idProducto)
                .orElseThrow(() -> new InventoryException("No existe inventario para producto " + idProducto));
        // A second request with the same order key may have reserved while this call waited for the row lock.
        existing = reservationRepository.findByOrderIdAndProductId(orderId, idProducto);
        if (existing.isPresent()) {
            InventoryReservation reservation = existing.get();
            if (!reservation.getQuantity().equals(cantidad)) throw new InventoryException("La clave de idempotencia ya se usó con otra cantidad");
            if ("LIBERADA".equals(reservation.getStatus())) throw new InventoryException("La reserva fue liberada y no puede reabrirse para esta orden");
            return reservation;
        }
        if (inventory.getStockDisponible() < cantidad) throw new InventoryException("Stock insuficiente para producto " + idProducto);
        inventory.setStockDisponible(inventory.getStockDisponible() - cantidad);
        inventory.setStockReservado(inventory.getStockReservado() + cantidad);
        inventoryRepository.save(inventory);
        InventoryReservation reservation = new InventoryReservation();
        reservation.setOrderId(orderId); reservation.setProductId(idProducto); reservation.setQuantity(cantidad); reservation.setStatus("RESERVADA");
        reservationRepository.save(reservation);
        recordOrderMovement(orderId, idProducto, cantidad, "RESERVA");
        log.info("Reserva de inventario creada orderId={} productId={} quantity={}", orderId, idProducto, cantidad);
        return reservation;
    }

    @Override
    @Transactional
    public InventoryReservation liberarReserva(Long orderId, Long idProducto) {
        var found = reservationRepository.findByOrderIdAndProductIdForUpdate(orderId, idProducto);
        if (found.isEmpty()) return null; // Compensación repetida o timeout antes de reservar: no-op seguro.
        InventoryReservation reservation = found.get();
        if ("LIBERADA".equals(reservation.getStatus())) return reservation;
        Inventory inventory = inventoryRepository.findByIdProductoForUpdate(idProducto)
                .orElseThrow(() -> new InventoryException("No existe inventario para producto " + idProducto));
        if ("RESERVADA".equals(reservation.getStatus())) {
            if (inventory.getStockReservado() < reservation.getQuantity()) throw new InventoryException("Stock reservado inconsistente");
            inventory.setStockReservado(inventory.getStockReservado() - reservation.getQuantity());
        } else if (!"CONFIRMADA".equals(reservation.getStatus())) {
            throw new InventoryException("Estado de reserva no reconocido: " + reservation.getStatus());
        }
        // Si el pago confirmó parcialmente la reserva, esta operación revierte la salida en la compensación.
        inventory.setStockDisponible(inventory.getStockDisponible() + reservation.getQuantity());
        inventoryRepository.save(inventory);
        reservation.setStatus("LIBERADA"); reservation.setUpdatedAt(LocalDateTime.now()); reservationRepository.save(reservation);
        recordOrderMovement(orderId, idProducto, reservation.getQuantity(), "LIBERACION");
        log.info("Reserva de inventario compensada orderId={} productId={}", orderId, idProducto);
        return reservation;
    }

    @Override
    @Transactional
    public InventoryReservation confirmarReserva(Long orderId, Long idProducto) {
        InventoryReservation reservation = reservationRepository.findByOrderIdAndProductIdForUpdate(orderId, idProducto)
                .orElseThrow(() -> new InventoryException("No existe reserva para la orden y producto indicados"));
        if ("CONFIRMADA".equals(reservation.getStatus())) return reservation;
        if (!"RESERVADA".equals(reservation.getStatus())) throw new InventoryException("No se puede confirmar una reserva " + reservation.getStatus());
        Inventory inventory = inventoryRepository.findByIdProductoForUpdate(idProducto)
                .orElseThrow(() -> new InventoryException("No existe inventario para producto " + idProducto));
        if (inventory.getStockReservado() < reservation.getQuantity()) throw new InventoryException("Stock reservado inconsistente");
        inventory.setStockReservado(inventory.getStockReservado() - reservation.getQuantity());
        inventoryRepository.save(inventory);
        reservation.setStatus("CONFIRMADA"); reservation.setUpdatedAt(LocalDateTime.now()); reservationRepository.save(reservation);
        recordOrderMovement(orderId, idProducto, reservation.getQuantity(), "SALIDA");
        log.info("Reserva confirmada orderId={} productId={}", orderId, idProducto);
        return reservation;
    }

    private void recordOrderMovement(Long orderId, Long productId, Long quantity, String type) {
        MovimientoInventario movement = new MovimientoInventario();
        movement.setOrderId(orderId); movement.setIdProducto(productId); movement.setCantidad(quantity);
        movement.setTipo(type); movement.setFecha(LocalDateTime.now());
        movimientoInventarioRepository.save(movement);
    }

    @Override
    @Transactional
    public Inventory salidaStock(Long idProducto, Long cantidad) {
        Inventory inventory = inventoryRepository.findByIdProducto(idProducto)
                .stream().findFirst().orElseThrow(
                        () -> new InventoryException("No se encontró registro de inventario para el producto: " + idProducto));

        if (cantidad <= 0) {
            throw new InventoryException("La cantidad de salida definitiva debe ser superior a cero");
        }

        if (inventory.getStockReservado() < cantidad) {
            throw new InventoryException("Inconsistencia: No existen suficientes unidades reservadas asociadas a este pago");
        }

        inventory.setStockReservado(inventory.getStockReservado() - cantidad);

        MovimientoInventario mov = new MovimientoInventario();
        mov.setIdProducto(inventory.getIdProducto());
        mov.setTipo("SALIDA");
        mov.setCantidad(cantidad);
        mov.setFecha(LocalDateTime.now());
        movimientoInventarioRepository.save(mov);

        return inventoryRepository.save(inventory);
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        if (!inventoryRepository.existsById(id)) {
            throw new InventoryException("No es posible eliminar. El registro de inventario especificado no existe.");
        }
        inventoryRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Inventory> findByIdProducto(Long idProducto) {
        return inventoryRepository.findByIdProducto(idProducto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Inventory> findByUbicacion(String ubicacion) {
        return inventoryRepository.findByUbicacion(ubicacion);
    }
}