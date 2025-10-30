package com.foodtruck.backend.application.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.foodtruck.backend.application.dto.OrderDtos.*;
import com.foodtruck.backend.application.mapper.OrderMapper;
import com.foodtruck.backend.application.validator.OrderValidator;
import com.foodtruck.backend.domain.model.Order;
import com.foodtruck.backend.domain.model.OrderItem;
import com.foodtruck.backend.domain.model.Product;
import com.foodtruck.backend.domain.model.Promotion;
import com.foodtruck.backend.domain.model.User;
import com.foodtruck.backend.domain.repository.OrderItemRepository;
import com.foodtruck.backend.domain.repository.OrderRepository;
import com.foodtruck.backend.domain.repository.UserRepository;
import com.foodtruck.backend.domain.types.OrderItemType;
import com.foodtruck.backend.domain.types.OrderStatus;
import com.foodtruck.backend.presentation.exception.order.OrderExceptions.*;

import lombok.RequiredArgsConstructor;

/**
 * Servicio de negocio para la gestión completa de órdenes del sistema.
 * Se enfoca únicamente en la orquestación de la lógica de negocio,
 * delegando validaciones y transformaciones a componentes especializados.
 */
@Service
@Transactional
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final UserRepository userRepository;
    private final OrderValidator validator;
    private final OrderMapper mapper;

    /**
     * Crea una nueva orden para un usuario.
     * Orquesta la validación, construcción y persistencia de la orden con sus
     * items.
     */
    public OrderDetailResponse createOrder(CreateOrderRequest request, Long userId) {
        // Obtener el usuario
        User user = findUserById(userId);

        // Validar que la orden no esté vacía
        validator.validateOrderNotEmpty(request.items().size());

        // Validar límite de órdenes activas (opcional)
        int activeOrdersCount = (int) orderRepository.countByUserAndStatus(user, OrderStatus.PENDIENTE)
                + (int) orderRepository.countByUserAndStatus(user, OrderStatus.EN_PREPARACION);
        validator.validateUserActiveOrdersLimit(user, activeOrdersCount);

        // Crear la orden
        Order order = mapper.toOrder(request);
        order.setUser(user);

        // Procesar items y calcular totales
        List<OrderItem> orderItems = processOrderItems(request.items(), order);
        order.setItems(orderItems);

        // Calcular subtotal y total
        BigDecimal subtotal = calculateSubtotal(orderItems);
        order.setSubtotal(subtotal);
        order.setTotal(subtotal.add(request.tip() != null ? request.tip() : BigDecimal.ZERO));

        // Persistir
        Order savedOrder = orderRepository.save(order);

        return mapper.toOrderDetailResponse(savedOrder);
    }

    /**
     * Actualiza completamente una orden existente (solo si está en estado PENDIENTE
     * o en los primeros minutos de EN_PREPARACION).
     */
    public OrderDetailResponse updateOrder(Long orderId, CreateOrderRequest request, Long userId) {
        Order order = findOrderByIdWithItems(orderId);
        User user = findUserById(userId);

        // Validar permisos y estado
        validator.validateUserOwnsOrder(order, user);
        validator.validateOrderCanBeModified(order);

        // Validar que la orden no esté vacía
        validator.validateOrderNotEmpty(request.items().size());

        // Eliminar items existentes
        orderItemRepository.deleteByOrder(order);
        order.getItems().clear();

        // Procesar nuevos items
        List<OrderItem> orderItems = processOrderItems(request.items(), order);
        order.setItems(orderItems);

        // Recalcular totales
        BigDecimal subtotal = calculateSubtotal(orderItems);
        order.setSubtotal(subtotal);
        order.setTip(request.tip() != null ? request.tip() : BigDecimal.ZERO);
        order.setTotal(subtotal.add(order.getTip()));

        // Persistir
        Order savedOrder = orderRepository.save(order);

        // TODO: Notificar a todos los empleados (por WebSocket) que la orden fue
        // modificada por el usuario.
        // Ejemplo: messagingTemplate.convertAndSend("/topic/orders/updates",
        // mapper.toOrderDetailResponse(savedOrder));

        return mapper.toOrderDetailResponse(savedOrder);
    }

    /**
     * Actualiza el estado de una orden.
     * TODO: Implementar notificación por WebSocket cuando el estado cambie
     */
    public OrderDetailResponse updateOrderStatus(Long orderId, UpdateOrderStatusRequest request) {
        Order order = findOrderByIdWithItems(orderId);

        // Validar transición de estado
        validator.validateOrderStatusTransition(order, request.status());

        // Actualizar estado
        order.setStatus(request.status());

        // Si el estado es ENTREGADO, registrar fecha de entrega
        if (request.status() == OrderStatus.ENTREGADO) {
            order.setDeliveredAt(LocalDateTime.now());
        }

        // Si el estado es CANCELADO, registrar fecha de cancelación
        if (request.status() == OrderStatus.CANCELADO) {
            order.setCanceledAt(LocalDateTime.now());
        }

        Order savedOrder = orderRepository.save(order);

        // TODO: Enviar notificación WebSocket sobre el cambio de estado
        // messagingTemplate.convertAndSend("/topic/orders/" + orderId,
        // mapper.toOrderDetailResponse(savedOrder));

        return mapper.toOrderDetailResponse(savedOrder);
    }

    /**
     * Actualiza el tiempo estimado de una orden.
     * TODO: Implementar notificación por WebSocket cuando el tiempo estimado cambie
     */
    public OrderDetailResponse updateOrderEstimatedTime(Long orderId, UpdateOrderEstimatedTimeRequest request) {
        Order order = findOrderById(orderId);

        // Validar formato del tiempo estimado
        validator.validateEstimatedTime(request.estimatedTime());

        // Actualizar tiempo estimado
        order.setEstimatedTime(request.estimatedTime());

        Order savedOrder = orderRepository.save(order);

        // TODO: Enviar notificación WebSocket sobre el cambio de tiempo estimado
        // messagingTemplate.convertAndSend("/topic/orders/" + orderId,
        // mapper.toOrderDetailResponse(savedOrder));

        return mapper.toOrderDetailResponse(savedOrder);
    }

    /**
     * Actualiza la propina de una orden.
     */
    public OrderDetailResponse updateOrderTip(Long orderId, UpdateOrderTipRequest request, Long userId) {
        Order order = findOrderByIdWithItems(orderId);
        User user = findUserById(userId);

        // Validar permisos y estado
        validator.validateUserOwnsOrder(order, user);
        validator.validateOrderCanBeModified(order);

        // Actualizar propina y recalcular total
        order.setTip(request.tip());
        order.setTotal(order.getSubtotal().add(request.tip()));

        Order savedOrder = orderRepository.save(order);

        return mapper.toOrderDetailResponse(savedOrder);
    }

    /**
     * Cancela una orden (el usuario cambia el estado a CANCELADO).
     */
    public OrderCancelledResponse cancelOrder(Long orderId, Long userId) {
        Order order = findOrderById(orderId);
        User user = findUserById(userId);

        // Validar permisos y que la orden pueda ser cancelada
        validator.validateUserOwnsOrder(order, user);
        validator.validateOrderCanBeCancelled(order);

        // Actualizar estado y fecha de cancelación
        order.setStatus(OrderStatus.CANCELADO);
        order.setCanceledAt(LocalDateTime.now());

        Order savedOrder = orderRepository.save(order);

        return mapper.toOrderCancelledResponse(savedOrder);
    }

    /**
     * Obtiene una orden por ID con todos sus detalles.
     */
    @Transactional(readOnly = true)
    public OrderDetailResponse getOrderById(Long orderId) {
        Order order = orderRepository.findByIdWithItems(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));
        return mapper.toOrderDetailResponse(order);
    }

    /**
     * Obtiene una orden por ID validando que pertenezca al usuario.
     */
    @Transactional(readOnly = true)
    public OrderDetailResponse getOrderByIdForUser(Long orderId, Long userId) {
        Order order = orderRepository.findByIdWithItems(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));
        User user = findUserById(userId);

        validator.validateUserOwnsOrder(order, user);

        return mapper.toOrderDetailResponse(order);
    }

    /**
     * Obtiene todas las órdenes de un usuario con paginación.
     */
    @Transactional(readOnly = true)
    public Page<OrderSummaryResponse> getUserOrders(Long userId, Pageable pageable) {
        User user = findUserById(userId);
        Page<Order> orders = orderRepository.findByUser(user, pageable);
        return mapper.toOrderSummaryResponsePage(orders);
    }

    /**
     * Obtiene las órdenes de un usuario ordenadas por fecha descendente.
     */
    @Transactional(readOnly = true)
    public List<OrderSummaryResponse> getUserOrdersOrderedByDate(Long userId) {
        User user = findUserById(userId);
        List<Order> orders = orderRepository.findByUserOrderByCreatedAtDesc(user);
        return mapper.toOrderSummaryResponses(orders);
    }

    /**
     * Obtiene órdenes por estado con paginación.
     */
    @Transactional(readOnly = true)
    public Page<OrderSummaryResponse> getOrdersByStatus(OrderStatus status, Pageable pageable) {
        Page<Order> orders = orderRepository.findByStatus(status, pageable);
        return mapper.toOrderSummaryResponsePage(orders);
    }

    /**
     * Obtiene órdenes de un usuario por estado con paginación.
     */
    @Transactional(readOnly = true)
    public Page<OrderSummaryResponse> getUserOrdersByStatus(Long userId, OrderStatus status, Pageable pageable) {
        User user = findUserById(userId);
        Page<Order> orders = orderRepository.findByUserAndStatus(user, status, pageable);
        return mapper.toOrderSummaryResponsePage(orders);
    }

    /**
     * Obtiene órdenes dentro de un rango de fechas.
     */
    @Transactional(readOnly = true)
    public Page<OrderSummaryResponse> getOrdersByDateRange(LocalDateTime startDate, LocalDateTime endDate,
            Pageable pageable) {
        Page<Order> orders = orderRepository.findByDateRange(startDate, endDate, pageable);
        return mapper.toOrderSummaryResponsePage(orders);
    }

    /**
     * Obtiene órdenes de un usuario dentro de un rango de fechas.
     */
    @Transactional(readOnly = true)
    public Page<OrderSummaryResponse> getUserOrdersByDateRange(Long userId, LocalDateTime startDate,
            LocalDateTime endDate, Pageable pageable) {
        User user = findUserById(userId);
        Page<Order> orders = orderRepository.findByUserAndDateRange(user, startDate, endDate, pageable);
        return mapper.toOrderSummaryResponsePage(orders);
    }

    /**
     * Obtiene todas las órdenes activas (no canceladas ni entregadas).
     */
    @Transactional(readOnly = true)
    public Page<OrderSummaryResponse> getActiveOrders(Pageable pageable) {
        Page<Order> orders = orderRepository.findActiveOrders(pageable);
        return mapper.toOrderSummaryResponsePage(orders);
    }

    /**
     * Obtiene la última orden de un usuario.
     */
    @Transactional(readOnly = true)
    public OrderDetailResponse getLastUserOrder(Long userId) {
        User user = findUserById(userId);
        Order order = orderRepository.findFirstByUserOrderByCreatedAtDesc(user)
                .orElseThrow(() -> new OrderNotFoundException("No se encontraron órdenes para este usuario"));
        return mapper.toOrderDetailResponse(order);
    }

    /**
     * Obtiene órdenes pendientes de un usuario.
     */
    @Transactional(readOnly = true)
    public List<OrderSummaryResponse> getUserPendingOrders(Long userId) {
        User user = findUserById(userId);
        List<Order> orders = orderRepository.findByUserAndStatusOrderByCreatedAtDesc(user, OrderStatus.PENDIENTE);
        return mapper.toOrderSummaryResponses(orders);
    }

    /**
     * Obtiene estadísticas generales de órdenes.
     */
    @Transactional(readOnly = true)
    public OrderStatisticsResponse getOrderStatistics() {
        long totalOrders = orderRepository.count();
        long pendingOrders = orderRepository.countByStatus(OrderStatus.PENDIENTE);
        long completedOrders = orderRepository.countByStatus(OrderStatus.ENTREGADO);
        long cancelledOrders = orderRepository.countByStatus(OrderStatus.CANCELADO);

        BigDecimal totalRevenue = orderRepository
                .getTotalSalesByDateRange(LocalDateTime.now().minusYears(100), LocalDateTime.now())
                .orElse(BigDecimal.ZERO);

        return mapper.toOrderStatisticsResponse(totalOrders, pendingOrders, completedOrders, cancelledOrders,
                totalRevenue);
    }

    /**
     * Obtiene estadísticas de órdenes por rango de fechas.
     */
    @Transactional(readOnly = true)
    public OrderStatisticsResponse getOrderStatisticsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        // Para este método necesitarías agregar más consultas específicas en el
        // repositorio
        // Por ahora retornamos estadísticas básicas
        BigDecimal totalRevenue = orderRepository.getTotalSalesByDateRange(startDate, endDate)
                .orElse(BigDecimal.ZERO);

        return mapper.toOrderStatisticsResponse(0L, 0L, 0L, 0L, totalRevenue);
    }

    /**
     * Obtiene los productos más vendidos.
     */
    @Transactional(readOnly = true)
    public List<TopSellingItemResponse> getTopSellingProducts() {
        List<Object[]> results = orderItemRepository.findTopSellingProducts();
        return results.stream()
                .map(data -> mapper.toTopSellingItemResponse(data, OrderItemType.PRODUCT))
                .collect(Collectors.toList());
    }

    /**
     * Obtiene las promociones más vendidas.
     */
    @Transactional(readOnly = true)
    public List<TopSellingItemResponse> getTopSellingPromotions() {
        List<Object[]> results = orderItemRepository.findTopSellingPromotions();
        return results.stream()
                .map(data -> mapper.toTopSellingItemResponse(data, OrderItemType.PROMOTION))
                .collect(Collectors.toList());
    }

    /**
     * Verifica si un usuario tiene órdenes activas.
     */
    @Transactional(readOnly = true)
    public boolean userHasActiveOrders(Long userId) {
        User user = findUserById(userId);
        List<OrderStatus> activeStatuses = List.of(OrderStatus.PENDIENTE, OrderStatus.EN_PREPARACION);
        return orderRepository.existsByUserAndStatusIn(user, activeStatuses);
    }

    // ========== MÉTODOS PRIVADOS DE ORQUESTACIÓN ==========

    private User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario con ID " + userId + " no encontrado"));
    }

    private Order findOrderById(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));
    }

    private Order findOrderByIdWithItems(Long orderId) {
        return orderRepository.findByIdWithItems(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));
    }

    private List<OrderItem> processOrderItems(List<CreateOrderItemRequest> itemRequests, Order order) {
        List<OrderItem> orderItems = new ArrayList<>();

        for (CreateOrderItemRequest itemRequest : itemRequests) {
            Product product = null;
            Promotion promotion = null;

            // Validar y obtener producto o promoción según el tipo
            if (itemRequest.type() == OrderItemType.PRODUCT) {
                if (itemRequest.productId() == null) {
                    throw new InvalidOrderItemException("El productId es obligatorio para items de tipo PRODUCT");
                }
                product = validator.validateAndGetProduct(itemRequest.productId());
            } else if (itemRequest.type() == OrderItemType.PROMOTION) {
                if (itemRequest.promotionId() == null) {
                    throw new InvalidOrderItemException("El promotionId es obligatorio para items de tipo PROMOTION");
                }
                promotion = validator.validateAndGetPromotion(itemRequest.promotionId());
            }

            // Crear el item
            OrderItem orderItem = mapper.toOrderItem(itemRequest, order, product, promotion);
            orderItems.add(orderItem);
        }

        return orderItems;
    }

    private BigDecimal calculateSubtotal(List<OrderItem> orderItems) {
        return orderItems.stream()
                .map(OrderItem::getLineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}