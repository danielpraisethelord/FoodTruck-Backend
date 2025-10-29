package com.foodtruck.backend.domain.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.foodtruck.backend.domain.model.Order;
import com.foodtruck.backend.domain.model.User;
import com.foodtruck.backend.domain.types.OrderStatus;

public interface OrderRepository extends JpaRepository<Order, Long> {

    /**
     * Encuentra todas las órdenes de un usuario específico
     */
    Page<Order> findByUser(User user, Pageable pageable);

    /**
     * Encuentra todas las órdenes de un usuario específico ordenadas por fecha de
     * creación descendente
     */
    List<Order> findByUserOrderByCreatedAtDesc(User user);

    /**
     * Encuentra todas las órdenes por estado
     */
    Page<Order> findByStatus(OrderStatus status, Pageable pageable);

    /**
     * Encuentra todas las órdenes de un usuario por estado
     */
    Page<Order> findByUserAndStatus(User user, OrderStatus status, Pageable pageable);

    /**
     * Encuentra órdenes dentro de un rango de fechas
     */
    @Query("SELECT o FROM Order o WHERE o.createdAt BETWEEN :startDate AND :endDate")
    Page<Order> findByDateRange(@Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);

    /**
     * Encuentra órdenes de un usuario dentro de un rango de fechas
     */
    @Query("SELECT o FROM Order o WHERE o.user = :user AND o.createdAt BETWEEN :startDate AND :endDate")
    Page<Order> findByUserAndDateRange(@Param("user") User user,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);

    /**
     * Encuentra una orden con sus items cargados (evita N+1 queries)
     */
    @Query("SELECT o FROM Order o LEFT JOIN FETCH o.items WHERE o.id = :orderId")
    Optional<Order> findByIdWithItems(@Param("orderId") Long orderId);

    /**
     * Encuentra todas las órdenes activas (no canceladas ni entregadas)
     */
    @Query("SELECT o FROM Order o WHERE o.status NOT IN ('CANCELADO', 'ENTREGADO')")
    Page<Order> findActiveOrders(Pageable pageable);

    /**
     * Cuenta las órdenes de un usuario por estado
     */
    long countByUserAndStatus(User user, OrderStatus status);

    /**
     * Verifica si existe una orden activa (pendiente o en preparación) para un
     * usuario
     */
    boolean existsByUserAndStatusIn(User user, List<OrderStatus> statuses);

    /**
     * Encuentra la última orden de un usuario
     */
    Optional<Order> findFirstByUserOrderByCreatedAtDesc(User user);

    /**
     * Encuentra órdenes pendientes de un usuario
     */
    List<Order> findByUserAndStatusOrderByCreatedAtDesc(User user, OrderStatus status);

    /**
     * Obtiene el total de ventas en un rango de fechas
     */
    @Query("SELECT SUM(o.total) FROM Order o WHERE o.status = 'ENTREGADO' AND o.deliveredAt BETWEEN :startDate AND :endDate")
    Optional<java.math.BigDecimal> getTotalSalesByDateRange(@Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    /**
     * Cuenta órdenes por estado
     */
    long countByStatus(OrderStatus status);
}