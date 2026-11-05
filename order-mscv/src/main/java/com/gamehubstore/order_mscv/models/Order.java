package com.gamehubstore.order_mscv.models;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Setter
@Getter
@ToString
@NoArgsConstructor
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long orderId;

    @NotNull(message = "El idUser es obligatorio")
    @Column(nullable = false, name = "user_id")
    private Long userId;

    @NotBlank(message = "El estado no puede ser vacío")
    @Column(nullable = false)
    private String estado; // CREADA, PAGADA, CANCELADA, EN_DESPACHO

    @PositiveOrZero(message = "El subtotal debe ser mayor o igual a 0")
    @Column(nullable = false)
    private Double subtotal;

    @PositiveOrZero(message = "El descuento debe ser mayor o igual a 0")
    private Double descuento;

    @Column(name = "promotion_code")
    private String promotionCode;

    @Column(name = "compensation_target")
    private String compensationTarget;

    @Column(name = "saga_updated_at")
    private LocalDateTime sagaUpdatedAt;

    @Positive(message = "El total debe ser mayor a 0")
    @Column(nullable = false)
    private Double total;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @PastOrPresent
    private LocalDateTime fecha;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "order_details", joinColumns = @JoinColumn(name = "order_id"))
    private List<DetailOrder> details = new ArrayList<>();

    @Embedded // Los campos de la clase Audit se guardan como columnas en la tabla 'orders'
    private Audit audit = new Audit();


}