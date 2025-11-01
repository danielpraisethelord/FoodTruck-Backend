package com.foodtruck.backend.domain.model;

import java.math.BigDecimal;

import com.foodtruck.backend.domain.types.OrderItemType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "order_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "order_id")
    private Order order;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderItemType type;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    @ManyToOne
    @JoinColumn(name = "promotion_id")
    private Promotion promotion;

    @Column(nullable = false)
    private Integer quantity;

    // Precio unitario congelado al momento de la venta
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice;

    // Total de esta línea (unitPrice * quantity)
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal lineTotal;

    // Nombre congelado (para historial si se elimina el producto/promoción)
    @Column(nullable = false)
    private String itemName;
}
