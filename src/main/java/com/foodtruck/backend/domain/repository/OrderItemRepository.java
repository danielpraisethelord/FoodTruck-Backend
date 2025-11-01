package com.foodtruck.backend.domain.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.foodtruck.backend.domain.model.Order;
import com.foodtruck.backend.domain.model.OrderItem;
import com.foodtruck.backend.domain.model.Product;
import com.foodtruck.backend.domain.model.Promotion;
import com.foodtruck.backend.domain.types.OrderItemType;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

        /**
         * Encuentra todos los items de una orden específica
         */
        List<OrderItem> findByOrder(Order order);

        /**
         * Encuentra items por tipo (PRODUCT o PROMOTION)
         */
        List<OrderItem> findByOrderAndType(Order order, OrderItemType type);

        /**
         * Encuentra items que contengan un producto específico
         */
        List<OrderItem> findByProduct(Product product);

        /**
         * Encuentra items que contengan una promoción específica
         */
        List<OrderItem> findByPromotion(Promotion promotion);

        /**
         * Elimina todos los items de una orden
         */
        void deleteByOrder(Order order);

        /**
         * Cuenta los items de una orden
         */
        long countByOrder(Order order);

        /**
         * Obtiene los productos más vendidos
         */
        @Query("SELECT oi.product.id, oi.product.name, SUM(oi.quantity) as totalSold " +
                        "FROM OrderItem oi " +
                        "WHERE oi.type = 'PRODUCT' AND oi.order.status = 'ENTREGADO' " +
                        "GROUP BY oi.product.id, oi.product.name " +
                        "ORDER BY totalSold DESC")
        List<Object[]> findTopSellingProducts();

        /**
         * Obtiene las promociones más vendidas
         */
        @Query("SELECT oi.promotion.id, oi.promotion.name, SUM(oi.quantity) as totalSold " +
                        "FROM OrderItem oi " +
                        "WHERE oi.type = 'PROMOTION' AND oi.order.status = 'ENTREGADO' " +
                        "GROUP BY oi.promotion.id, oi.promotion.name " +
                        "ORDER BY totalSold DESC")
        List<Object[]> findTopSellingPromotions();

        /**
         * Verifica si un producto está siendo usado en alguna orden activa
         */
        @Query("SELECT CASE WHEN COUNT(oi) > 0 THEN true ELSE false END " +
                        "FROM OrderItem oi " +
                        "WHERE oi.product = :product AND oi.order.status NOT IN ('CANCELADO', 'ENTREGADO')")
        boolean isProductInActiveOrders(@Param("product") Product product);

        /**
         * Verifica si una promoción está siendo usada en alguna orden activa
         */
        @Query("SELECT CASE WHEN COUNT(oi) > 0 THEN true ELSE false END " +
                        "FROM OrderItem oi " +
                        "WHERE oi.promotion = :promotion AND oi.order.status NOT IN ('CANCELADO', 'ENTREGADO')")
        boolean isPromotionInActiveOrders(@Param("promotion") Promotion promotion);
}