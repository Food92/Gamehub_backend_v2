package com.gamehubstore.inventory_mscv.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "inventory_reservations", uniqueConstraints = @UniqueConstraint(
        name = "uk_inventory_reservation_order_product", columnNames = {"order_id", "product_id"}))
public class InventoryReservation {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "order_id", nullable = false) private Long orderId;
    @Column(name = "product_id", nullable = false) private Long productId;
    @Column(nullable = false) private Long quantity;
    @Column(nullable = false) private String status;
    @Column(nullable = false) private LocalDateTime updatedAt = LocalDateTime.now();
    public Long getId(){return id;} public Long getOrderId(){return orderId;} public void setOrderId(Long v){orderId=v;}
    public Long getProductId(){return productId;} public void setProductId(Long v){productId=v;} public Long getQuantity(){return quantity;} public void setQuantity(Long v){quantity=v;}
    public String getStatus(){return status;} public void setStatus(String v){status=v;} public LocalDateTime getUpdatedAt(){return updatedAt;} public void setUpdatedAt(LocalDateTime v){updatedAt=v;}
}
