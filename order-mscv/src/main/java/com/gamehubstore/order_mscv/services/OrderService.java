package com.gamehubstore.order_mscv.services;

import com.gamehubstore.order_mscv.models.Order;
import com.gamehubstore.order_mscv.models.dtos.OrderDTO;

import java.util.List;

public interface OrderService {
    OrderDTO save(OrderDTO orderDTO);
    Order update(OrderDTO dto, Long orderId);
    Order findById(Long orderId);
    List<Order> findAll();
    List<Order> findByUserId(Long userId);
    List<Order> findByEstado(String estado);
    Order cancelOrder(Long orderId);
    OrderDTO findByIdDTO(Long orderId);
    OrderDTO updateEstado(Long orderId, String estado);
}