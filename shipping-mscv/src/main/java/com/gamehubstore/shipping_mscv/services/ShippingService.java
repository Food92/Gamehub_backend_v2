package com.gamehubstore.shipping_mscv.services;

import com.gamehubstore.shipping_mscv.models.Shipping;
import com.gamehubstore.shipping_mscv.models.dtos.CancelShippingDTO;
import com.gamehubstore.shipping_mscv.models.dtos.ShippingDTO;

import java.util.List;

public interface ShippingService {
    ShippingDTO save(ShippingDTO shippingDTO);
    Shipping update(ShippingDTO shippingDTO, Long shippingId);
    void cancel(CancelShippingDTO cancelShippingDTO);

    List<Shipping> findAll();
    List<Shipping> findByUserId(Long userId);
    List<Shipping> findByOrderId(Long orderId);
    List<Shipping> findByEstado(String estado);
}