package com.gamehubstore.shipping_mscv.services.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gamehubstore.shipping_mscv.client.OrderClient;
import com.gamehubstore.shipping_mscv.client.UserClient;
import com.gamehubstore.shipping_mscv.exceptions.ResourceNotFoundException;
import com.gamehubstore.shipping_mscv.exceptions.ShippingException;
import com.gamehubstore.shipping_mscv.models.Shipping;
import com.gamehubstore.shipping_mscv.models.dtos.CancelShippingDTO;
import com.gamehubstore.shipping_mscv.models.dtos.OrderDTO;
import com.gamehubstore.shipping_mscv.models.dtos.ShippingDTO;
import com.gamehubstore.shipping_mscv.models.dtos.UserDTO;
import com.gamehubstore.shipping_mscv.repositories.RepositoryShipping;
import com.gamehubstore.shipping_mscv.services.ShippingService;
import feign.FeignException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class ServiceShippingImpl implements ShippingService {
    private static final Logger log = LoggerFactory.getLogger(ServiceShippingImpl.class);

    private RepositoryShipping repositoryShipping;
    private  OrderClient orderClient;
    private  UserClient userClient;


    @Override
    @Transactional
    public ShippingDTO save(ShippingDTO shippingDTO) {
        log.info("operation={} started", "save");
        // REGLA 1: Consumir order-service para validar que la orden exista y esté PAGADA
        try {
            OrderDTO orderRemote = orderClient.getOrderById(shippingDTO.getOrderId());
            if (orderRemote == null || !"PAGADA".equalsIgnoreCase(orderRemote.getEstado())) {
                throw new ShippingException("No se puede generar un despacho para una orden que no está en estado PAGADA.");
            }
        } catch (FeignException.NotFound e) {
            throw new ResourceNotFoundException("La orden con ID " + shippingDTO.getOrderId() + " no existe.");
        }

        // REGLA 2: Consumir user-service para validar que el cliente exista y tenga dirección válida
        try {
            UserDTO userRemote = userClient.getUserById(shippingDTO.getUserId());
            if (userRemote == null || userRemote.getDireccion() == null || userRemote.getDireccion().trim().isEmpty()) {
                throw new ShippingException("El cliente no posee una dirección válida registrada para la entrega.");
            }
            // Si el DTO de entrada no provee dirección, heredamos automáticamente la del perfil del usuario
            if (shippingDTO.getDireccion() == null || shippingDTO.getDireccion().trim().isEmpty()) {
                shippingDTO.setDireccion(userRemote.getDireccion());
            }
        } catch (FeignException.NotFound e) {
            throw new ResourceNotFoundException("El usuario con ID " + shippingDTO.getUserId() + " no existe.");
        }

        // Construcción de la entidad final
        Shipping shipping = new Shipping();
        shipping.setOrderId(shippingDTO.getOrderId());
        shipping.setUserId(shippingDTO.getUserId());
        shipping.setDireccion(shippingDTO.getDireccion());
        shipping.setTransportista(shippingDTO.getTransportista());
        shipping.setTracking(shippingDTO.getTracking());
        // Nota: El estado "CREADO" y la "fechaEnvio" se asignan solas en el @PrePersist de la entidad

        Shipping guardado = repositoryShipping.save(shipping);

        // Avanzar sincrónicamente el flujo en el microservicio de órdenes
        orderClient.updateEstado(guardado.getOrderId(), "EN_DESPACHO");

        // Rellenar DTO de respuesta para confirmación del controlador
        shippingDTO.setShippingId(guardado.getShippingId());
        shippingDTO.setEstado(guardado.getEstado());
        shippingDTO.setFechaEnvio(guardado.getFechaEnvio());

        return shippingDTO;
    }

    @Override
    @Transactional
    public Shipping update(ShippingDTO shippingDTO, Long shippingId) {
        log.info("operation={} started", "update");
        Shipping existing = repositoryShipping.findById(shippingId)
                .orElseThrow(() -> new ResourceNotFoundException("Despacho no encontrado con el ID: " + shippingId));

        // REGLA 3: Validar que el número de tracking sea único a nivel de sistema si se envía uno
        if (shippingDTO.getTracking() != null && !shippingDTO.getTracking().trim().isEmpty()) {
            repositoryShipping.findByTracking(shippingDTO.getTracking().trim()).ifPresent(s -> {
                if (!s.getShippingId().equals(shippingId)) {
                    throw new ShippingException("El número de tracking '" + shippingDTO.getTracking() + "' ya está asignado a otro despacho.");
                }
            });
            existing.setTracking(shippingDTO.getTracking().trim());
        }

        // Gestión de transiciones de estado de la empresa transportista
        if (shippingDTO.getEstado() != null && !shippingDTO.getEstado().trim().isEmpty()) {
            String nuevoEstado = shippingDTO.getEstado().toUpperCase().trim();

            // REGLA 4: No cambiar a entregado sin inyectar la fecha de entrega
            if ("ENTREGADO".equals(nuevoEstado)) {
                existing.setFechaEntrega(LocalDate.now());
                // Cerramos el ciclo logístico notificando a la orden
                orderClient.updateEstado(existing.getOrderId(), "ENTREGADA");
            }
            existing.setEstado(nuevoEstado);
        }

        // Campos mutables secundarios
        if (shippingDTO.getDireccion() != null && !shippingDTO.getDireccion().trim().isEmpty()) {
            existing.setDireccion(shippingDTO.getDireccion());
        }
        if (shippingDTO.getTransportista() != null && !shippingDTO.getTransportista().trim().isEmpty()) {
            existing.setTransportista(shippingDTO.getTransportista());
        }

        return repositoryShipping.save(existing);
    }

    @Override
    @Transactional
    public void cancel(CancelShippingDTO cancelShippingDTO) {
        Shipping shipping = repositoryShipping.findById(cancelShippingDTO.getShippingId())
                .orElseThrow(() -> new ResourceNotFoundException("Despacho no encontrado con ID: " + cancelShippingDTO.getShippingId()));

        // Validación lógica de negocio: No se puede cancelar algo que ya se entregó al comprador
        if ("ENTREGADO".equalsIgnoreCase(shipping.getEstado())) {
            throw new ShippingException("No se puede cancelar un despacho que ya figura en el sistema como ENTREGADO.");
        }

        shipping.setEstado("CANCELADO");
        repositoryShipping.save(shipping);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Shipping> findAll() {
        return repositoryShipping.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Shipping> findByUserId(Long userId) {
        return repositoryShipping.findByUserId(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Shipping> findByOrderId(Long orderId) {
        return repositoryShipping.findByOrderId(orderId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Shipping> findByEstado(String estado) {
        return repositoryShipping.findByEstado(estado.toUpperCase().trim());
    }
}