package com.foodtruck.backend.presentation.controller;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.foodtruck.backend.application.dto.OrderDtos.*;
import com.foodtruck.backend.application.service.OrderService;
import com.foodtruck.backend.domain.model.User;
import com.foodtruck.backend.domain.types.OrderStatus;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@RestController
@RequestMapping("/api/orders")
@Validated
@Tag(name = "Orders", description = "Gestión completa de órdenes del foodtruck")
public class OrderController {

        @Autowired
        private OrderService orderService;

        private static final String ADMIN_OR_EMPLOYEE = "hasRole('ADMIN') or hasRole('EMPLOYEE')";
        private static final String AUTHENTICATED = "isAuthenticated()";

        // ========== OPERACIONES CRUD ==========

        @Operation(summary = "Crear una nueva orden", description = "Crea una nueva orden para el usuario autenticado. La orden se crea en estado PENDIENTE.", security = @SecurityRequirement(name = "bearerAuth"), responses = {
                        @ApiResponse(responseCode = "201", description = "Orden creada correctamente", content = @Content(mediaType = "application/json", schema = @Schema(implementation = OrderDetailResponse.class))),
                        @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos", content = @Content(mediaType = "application/json", examples = {
                                        @ExampleObject(name = "Orden vacía", value = """
                                                        {
                                                            "error": "EMPTY_ORDER",
                                                            "message": "La orden debe contener al menos un item"
                                                        }
                                                        """),
                                        @ExampleObject(name = "Configuración inválida de item", value = """
                                                        {
                                                            "error": "INVALID_ORDER_ITEM_CONFIGURATION",
                                                            "message": "El campo productId es obligatorio cuando el tipo es PRODUCT"
                                                        }
                                                        """)
                        })),
                        @ApiResponse(responseCode = "404", description = "Producto o promoción no encontrado", content = @Content(mediaType = "application/json", examples = {
                                        @ExampleObject(name = "Producto no disponible", value = """
                                                        {
                                                            "error": "PRODUCT_NOT_AVAILABLE",
                                                            "message": "El producto con id 123 no está disponible"
                                                        }
                                                        """),
                                        @ExampleObject(name = "Promoción no disponible", value = """
                                                        {
                                                            "error": "PROMOTION_NOT_AVAILABLE",
                                                            "message": "La promoción con id 456 no está disponible"
                                                        }
                                                        """)
                        })),
                        @ApiResponse(responseCode = "409", description = "Conflicto con reglas de negocio", content = @Content(mediaType = "application/json", examples = @ExampleObject(name = "Límite de órdenes activas", value = """
                                        {
                                            "error": "ORDER_LIMIT_EXCEEDED",
                                            "message": "No puedes tener más de 3 órdenes activas al mismo tiempo"
                                        }
                                        """)))
        })
        @PostMapping
        @PreAuthorize(AUTHENTICATED)
        public ResponseEntity<OrderDetailResponse> createOrder(
                        @Valid @RequestBody CreateOrderRequest request,
                        @AuthenticationPrincipal User user) {
                OrderDetailResponse order = orderService.createOrder(request, user.getId());
                return ResponseEntity.status(HttpStatus.CREATED).body(order);
        }

        @Operation(summary = "Actualizar una orden existente", description = "Actualiza completamente una orden existente. Solo se puede actualizar si está en estado PENDIENTE o en los primeros 5 minutos de estar EN_PREPARACION.", security = @SecurityRequirement(name = "bearerAuth"), responses = {
                        @ApiResponse(responseCode = "200", description = "Orden actualizada correctamente", content = @Content(mediaType = "application/json", schema = @Schema(implementation = OrderDetailResponse.class))),
                        @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos o la orden no puede ser modificada", content = @Content(mediaType = "application/json", examples = @ExampleObject(name = "Orden no modificable", value = """
                                        {
                                            "error": "ORDER_CANNOT_BE_MODIFIED",
                                            "message": "No se puede modificar la orden después de 5 minutos de estar en preparación"
                                        }
                                        """))),
                        @ApiResponse(responseCode = "403", description = "Sin permisos para modificar esta orden", content = @Content(mediaType = "application/json", examples = @ExampleObject(name = "Acceso denegado", value = """
                                        {
                                            "error": "ORDER_ACCESS_DENIED",
                                            "message": "No tienes permiso para acceder a esta orden"
                                        }
                                        """))),
                        @ApiResponse(responseCode = "404", description = "Orden no encontrada", content = @Content(mediaType = "application/json", examples = @ExampleObject(name = "Orden no encontrada", value = """
                                        {
                                            "error": "ORDER_NOT_FOUND",
                                            "message": "Orden con id 123 no encontrada"
                                        }
                                        """)))
        })
        @PutMapping("/{orderId}")
        @PreAuthorize(AUTHENTICATED)
        public ResponseEntity<OrderDetailResponse> updateOrder(
                        @Parameter(description = "ID de la orden", example = "1", required = true) @PathVariable @NotNull @Positive Long orderId,
                        @Valid @RequestBody CreateOrderRequest request,
                        @AuthenticationPrincipal User user) {
                OrderDetailResponse order = orderService.updateOrder(orderId, request, user.getId());
                return ResponseEntity.ok(order);
        }

        @Operation(summary = "Actualizar el estado de una orden", description = "Actualiza el estado de una orden. Solo empleados y administradores pueden realizar esta acción.", security = @SecurityRequirement(name = "bearerAuth"), responses = {
                        @ApiResponse(responseCode = "200", description = "Estado actualizado correctamente", content = @Content(mediaType = "application/json", schema = @Schema(implementation = OrderDetailResponse.class))),
                        @ApiResponse(responseCode = "400", description = "Transición de estado inválida", content = @Content(mediaType = "application/json", examples = @ExampleObject(name = "Transición inválida", value = """
                                        {
                                            "error": "INVALID_ORDER_STATUS_TRANSITION",
                                            "message": "Una orden PENDIENTE solo puede pasar a EN_PREPARACION o CANCELADO"
                                        }
                                        """))),
                        @ApiResponse(responseCode = "404", description = "Orden no encontrada", content = @Content(mediaType = "application/json", examples = @ExampleObject(name = "Orden no encontrada", value = """
                                        {
                                            "error": "ORDER_NOT_FOUND",
                                            "message": "Orden con id 123 no encontrada"
                                        }
                                        """)))
        })
        @PatchMapping("/{orderId}/status")
        @PreAuthorize(ADMIN_OR_EMPLOYEE)
        public ResponseEntity<OrderDetailResponse> updateOrderStatus(
                        @Parameter(description = "ID de la orden", example = "1", required = true) @PathVariable @NotNull @Positive Long orderId,
                        @Valid @RequestBody UpdateOrderStatusRequest request) {
                OrderDetailResponse order = orderService.updateOrderStatus(orderId, request);
                return ResponseEntity.ok(order);
        }

        @Operation(summary = "Actualizar el tiempo estimado de una orden", description = "Actualiza el tiempo estimado de entrega de una orden. Solo empleados y administradores pueden realizar esta acción.", security = @SecurityRequirement(name = "bearerAuth"), responses = {
                        @ApiResponse(responseCode = "200", description = "Tiempo estimado actualizado correctamente", content = @Content(mediaType = "application/json", schema = @Schema(implementation = OrderDetailResponse.class))),
                        @ApiResponse(responseCode = "400", description = "Formato de tiempo inválido", content = @Content(mediaType = "application/json", examples = @ExampleObject(name = "Formato inválido", value = """
                                        {
                                            "error": "INVALID_ESTIMATED_TIME",
                                            "message": "El tiempo estimado debe tener el formato MM:SS"
                                        }
                                        """))),
                        @ApiResponse(responseCode = "404", description = "Orden no encontrada")
        })
        @PatchMapping("/{orderId}/estimated-time")
        @PreAuthorize(ADMIN_OR_EMPLOYEE)
        public ResponseEntity<OrderDetailResponse> updateOrderEstimatedTime(
                        @Parameter(description = "ID de la orden", example = "1", required = true) @PathVariable @NotNull @Positive Long orderId,
                        @Valid @RequestBody UpdateOrderEstimatedTimeRequest request) {
                OrderDetailResponse order = orderService.updateOrderEstimatedTime(orderId, request);
                return ResponseEntity.ok(order);
        }

        @Operation(summary = "Actualizar la propina de una orden", description = "Actualiza la propina de una orden. Solo el usuario dueño de la orden puede realizar esta acción y solo si la orden está PENDIENTE o en los primeros minutos de EN_PREPARACION.", security = @SecurityRequirement(name = "bearerAuth"), responses = {
                        @ApiResponse(responseCode = "200", description = "Propina actualizada correctamente", content = @Content(mediaType = "application/json", schema = @Schema(implementation = OrderDetailResponse.class))),
                        @ApiResponse(responseCode = "400", description = "La orden no puede ser modificada", content = @Content(mediaType = "application/json", examples = @ExampleObject(name = "Orden no modificable", value = """
                                        {
                                            "error": "ORDER_CANNOT_BE_MODIFIED",
                                            "message": "No se puede modificar la orden después de 5 minutos de estar en preparación"
                                        }
                                        """))),
                        @ApiResponse(responseCode = "403", description = "Sin permisos para modificar esta orden", content = @Content(mediaType = "application/json", examples = @ExampleObject(name = "Acceso denegado", value = """
                                        {
                                            "error": "ORDER_ACCESS_DENIED",
                                            "message": "No tienes permiso para acceder a esta orden"
                                        }
                                        """))),
                        @ApiResponse(responseCode = "404", description = "Orden no encontrada", content = @Content(mediaType = "application/json", examples = @ExampleObject(name = "Orden no encontrada", value = """
                                        {
                                            "error": "ORDER_NOT_FOUND",
                                            "message": "Orden con id 123 no encontrada"
                                        }
                                        """)))
        })
        @PatchMapping("/{orderId}/tip")
        @PreAuthorize(AUTHENTICATED)
        public ResponseEntity<OrderDetailResponse> updateOrderTip(
                        @Parameter(description = "ID de la orden", example = "1", required = true) @PathVariable @NotNull @Positive Long orderId,
                        @Valid @RequestBody UpdateOrderTipRequest request,
                        @AuthenticationPrincipal User user) {
                OrderDetailResponse order = orderService.updateOrderTip(orderId, request, user.getId());
                return ResponseEntity.ok(order);
        }

        @Operation(summary = "Cancelar una orden", description = "Cancela una orden del usuario autenticado. Solo se puede cancelar si está en estado PENDIENTE o EN_PREPARACION.", security = @SecurityRequirement(name = "bearerAuth"), responses = {
                        @ApiResponse(responseCode = "200", description = "Orden cancelada correctamente", content = @Content(mediaType = "application/json", schema = @Schema(implementation = OrderCancelledResponse.class))),
                        @ApiResponse(responseCode = "400", description = "La orden no puede ser cancelada", content = @Content(mediaType = "application/json", examples = {
                                        @ExampleObject(name = "Orden ya cancelada", value = """
                                                        {
                                                            "error": "ORDER_ALREADY_CANCELLED",
                                                            "message": "La orden con id 123 ya está cancelada"
                                                        }
                                                        """),
                                        @ExampleObject(name = "Orden ya entregada", value = """
                                                        {
                                                            "error": "ORDER_ALREADY_DELIVERED",
                                                            "message": "No se puede cancelar una orden que ya fue entregada"
                                                        }
                                                        """)
                        })),
                        @ApiResponse(responseCode = "403", description = "Sin permisos para cancelar esta orden"),
                        @ApiResponse(responseCode = "404", description = "Orden no encontrada", content = @Content(mediaType = "application/json", examples = @ExampleObject(name = "Orden no encontrada", value = """
                                        {
                                            "error": "ORDER_NOT_FOUND",
                                            "message": "Orden con id 123 no encontrada"
                                        }
                                        """)))
        })
        @PatchMapping("/{orderId}/cancel")
        @PreAuthorize(AUTHENTICATED)
        public ResponseEntity<OrderCancelledResponse> cancelOrder(
                        @Parameter(description = "ID de la orden", example = "1", required = true) @PathVariable @NotNull @Positive Long orderId,
                        @AuthenticationPrincipal User user) {
                OrderCancelledResponse response = orderService.cancelOrder(orderId, user.getId());
                return ResponseEntity.ok(response);
        }

        // Me quede revisando aquí

        // ========== CONSULTAS BÁSICAS ==========

        @Operation(summary = "Obtener una orden por ID", description = "Obtiene los detalles completos de una orden específica. Los usuarios solo pueden ver sus propias órdenes, los empleados y administradores pueden ver cualquier orden.", security = @SecurityRequirement(name = "bearerAuth"), responses = {
                        @ApiResponse(responseCode = "200", description = "Orden encontrada", content = @Content(mediaType = "application/json", schema = @Schema(implementation = OrderDetailResponse.class))),
                        @ApiResponse(responseCode = "403", description = "Sin permisos para ver esta orden", content = @Content(mediaType = "application/json", examples = @ExampleObject(name = "Acceso denegado", value = """
                                        {
                                            "error": "ORDER_ACCESS_DENIED",
                                            "message": "No tienes permiso para acceder a esta orden"
                                        }
                                        """))),
                        @ApiResponse(responseCode = "404", description = "Orden no encontrada", content = @Content(mediaType = "application/json", examples = @ExampleObject(name = "Orden no encontrada", value = """
                                        {
                                            "error": "ORDER_NOT_FOUND",
                                            "message": "Orden con id 123 no encontrada"
                                        }
                                        """)))
        })
        @GetMapping("/{orderId}")
        @PreAuthorize(AUTHENTICATED)
        public ResponseEntity<OrderDetailResponse> getOrderById(
                        @Parameter(description = "ID de la orden", example = "1", required = true) @PathVariable @NotNull @Positive Long orderId,
                        @AuthenticationPrincipal User user) {
                // Si es empleado o admin, puede ver cualquier orden
                if (user.getAuthorities().stream()
                                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")
                                                || a.getAuthority().equals("ROLE_EMPLOYEE"))) {
                        return ResponseEntity.ok(orderService.getOrderById(orderId));
                }
                // Si es usuario normal, solo puede ver sus propias órdenes
                return ResponseEntity.ok(orderService.getOrderByIdForUser(orderId, user.getId()));
        }

        @Operation(summary = "Obtener órdenes del usuario autenticado", description = "Obtiene todas las órdenes del usuario autenticado con paginación.", security = @SecurityRequirement(name = "bearerAuth"), responses = {
                        @ApiResponse(responseCode = "200", description = "Órdenes obtenidas correctamente", content = @Content(mediaType = "application/json", schema = @Schema(implementation = OrderSummaryResponse.class)))
        })
        @GetMapping("/my-orders")
        @PreAuthorize(AUTHENTICATED)
        public ResponseEntity<Page<OrderSummaryResponse>> getMyOrders(
                        @PageableDefault(size = 10) Pageable pageable,
                        @AuthenticationPrincipal User user) {
                Page<OrderSummaryResponse> orders = orderService.getUserOrders(user.getId(), pageable);
                return ResponseEntity.ok(orders);
        }

        @Operation(summary = "Obtener órdenes del usuario ordenadas por fecha", description = "Obtiene todas las órdenes del usuario autenticado ordenadas por fecha de creación descendente.", security = @SecurityRequirement(name = "bearerAuth"), responses = {
                        @ApiResponse(responseCode = "200", description = "Órdenes obtenidas correctamente", content = @Content(mediaType = "application/json", schema = @Schema(implementation = OrderSummaryResponse.class)))
        })
        @GetMapping("/my-orders/recent")
        @PreAuthorize(AUTHENTICATED)
        public ResponseEntity<List<OrderSummaryResponse>> getMyOrdersOrderedByDate(
                        @AuthenticationPrincipal User user) {
                List<OrderSummaryResponse> orders = orderService.getUserOrdersOrderedByDate(user.getId());
                return ResponseEntity.ok(orders);
        }

        @Operation(summary = "Obtener la última orden del usuario", description = "Obtiene la orden más reciente del usuario autenticado.", security = @SecurityRequirement(name = "bearerAuth"), responses = {
                        @ApiResponse(responseCode = "200", description = "Orden obtenida correctamente", content = @Content(mediaType = "application/json", schema = @Schema(implementation = OrderDetailResponse.class))),
                        @ApiResponse(responseCode = "404", description = "No se encontraron órdenes para este usuario")
        })
        @GetMapping("/my-orders/last")
        @PreAuthorize(AUTHENTICATED)
        public ResponseEntity<OrderDetailResponse> getMyLastOrder(
                        @AuthenticationPrincipal User user) {
                OrderDetailResponse order = orderService.getLastUserOrder(user.getId());
                return ResponseEntity.ok(order);
        }

        @Operation(summary = "Obtener órdenes pendientes del usuario", description = "Obtiene todas las órdenes pendientes del usuario autenticado.", security = @SecurityRequirement(name = "bearerAuth"), responses = {
                        @ApiResponse(responseCode = "200", description = "Órdenes pendientes obtenidas correctamente", content = @Content(mediaType = "application/json", schema = @Schema(implementation = OrderSummaryResponse.class)))
        })
        @GetMapping("/my-orders/pending")
        @PreAuthorize(AUTHENTICATED)
        public ResponseEntity<List<OrderSummaryResponse>> getMyPendingOrders(
                        @AuthenticationPrincipal User user) {
                List<OrderSummaryResponse> orders = orderService.getUserPendingOrders(user.getId());
                return ResponseEntity.ok(orders);
        }

        // ========== CONSULTAS POR ESTADO (ADMIN/EMPLOYEE) ==========

        @Operation(summary = "Obtener órdenes por estado", description = "Obtiene todas las órdenes filtradas por estado con paginación. Solo empleados y administradores.", security = @SecurityRequirement(name = "bearerAuth"), responses = {
                        @ApiResponse(responseCode = "200", description = "Órdenes obtenidas correctamente", content = @Content(mediaType = "application/json", schema = @Schema(implementation = OrderSummaryResponse.class)))
        })
        @GetMapping("/status/{status}")
        @PreAuthorize(ADMIN_OR_EMPLOYEE)
        public ResponseEntity<Page<OrderSummaryResponse>> getOrdersByStatus(
                        @Parameter(description = "Estado de la orden", example = "PENDIENTE", required = true) @PathVariable OrderStatus status,
                        @PageableDefault(size = 10) Pageable pageable) {
                Page<OrderSummaryResponse> orders = orderService.getOrdersByStatus(status, pageable);
                return ResponseEntity.ok(orders);
        }

        @Operation(summary = "Obtener órdenes activas", description = "Obtiene todas las órdenes activas (no canceladas ni entregadas) con paginación. Solo empleados y administradores.", security = @SecurityRequirement(name = "bearerAuth"), responses = {
                        @ApiResponse(responseCode = "200", description = "Órdenes activas obtenidas correctamente", content = @Content(mediaType = "application/json", schema = @Schema(implementation = OrderSummaryResponse.class)))
        })
        @GetMapping("/active")
        @PreAuthorize(ADMIN_OR_EMPLOYEE)
        public ResponseEntity<Page<OrderSummaryResponse>> getActiveOrders(
                        @PageableDefault(size = 10) Pageable pageable) {
                Page<OrderSummaryResponse> orders = orderService.getActiveOrders(pageable);
                return ResponseEntity.ok(orders);
        }

        @Operation(summary = "Obtener órdenes de un usuario por estado", description = "Obtiene órdenes de un usuario específico filtradas por estado. Solo empleados y administradores.", security = @SecurityRequirement(name = "bearerAuth"), responses = {
                        @ApiResponse(responseCode = "200", description = "Órdenes obtenidas correctamente", content = @Content(mediaType = "application/json", schema = @Schema(implementation = OrderSummaryResponse.class)))
        })
        @GetMapping("/user/{userId}/status/{status}")
        @PreAuthorize(ADMIN_OR_EMPLOYEE)
        public ResponseEntity<Page<OrderSummaryResponse>> getUserOrdersByStatus(
                        @Parameter(description = "ID del usuario", example = "1", required = true) @PathVariable @NotNull @Positive Long userId,
                        @Parameter(description = "Estado de la orden", example = "EN_PREPARACION", required = true) @PathVariable OrderStatus status,
                        @PageableDefault(size = 10) Pageable pageable) {
                Page<OrderSummaryResponse> orders = orderService.getUserOrdersByStatus(userId, status, pageable);
                return ResponseEntity.ok(orders);
        }

        // ========== CONSULTAS POR FECHAS (ADMIN/EMPLOYEE) ==========

        @Operation(summary = "Obtener órdenes por rango de fechas", description = "Obtiene órdenes dentro de un rango de fechas específico. Solo empleados y administradores.", security = @SecurityRequirement(name = "bearerAuth"), responses = {
                        @ApiResponse(responseCode = "200", description = "Órdenes obtenidas correctamente", content = @Content(mediaType = "application/json", schema = @Schema(implementation = OrderSummaryResponse.class)))
        })
        @GetMapping("/date-range")
        @PreAuthorize(ADMIN_OR_EMPLOYEE)
        public ResponseEntity<Page<OrderSummaryResponse>> getOrdersByDateRange(
                        @Parameter(description = "Fecha de inicio", example = "2024-10-01T00:00:00", required = true) @RequestParam LocalDateTime startDate,
                        @Parameter(description = "Fecha de fin", example = "2024-10-31T23:59:59", required = true) @RequestParam LocalDateTime endDate,
                        @PageableDefault(size = 10) Pageable pageable) {
                Page<OrderSummaryResponse> orders = orderService.getOrdersByDateRange(startDate, endDate, pageable);
                return ResponseEntity.ok(orders);
        }

        @Operation(summary = "Obtener órdenes de un usuario por rango de fechas", description = "Obtiene órdenes de un usuario específico dentro de un rango de fechas. Solo empleados y administradores.", security = @SecurityRequirement(name = "bearerAuth"), responses = {
                        @ApiResponse(responseCode = "200", description = "Órdenes obtenidas correctamente", content = @Content(mediaType = "application/json", schema = @Schema(implementation = OrderSummaryResponse.class)))
        })
        @GetMapping("/user/{userId}/date-range")
        @PreAuthorize(ADMIN_OR_EMPLOYEE)
        public ResponseEntity<Page<OrderSummaryResponse>> getUserOrdersByDateRange(
                        @Parameter(description = "ID del usuario", example = "1", required = true) @PathVariable @NotNull @Positive Long userId,
                        @Parameter(description = "Fecha de inicio", example = "2024-10-01T00:00:00", required = true) @RequestParam LocalDateTime startDate,
                        @Parameter(description = "Fecha de fin", example = "2024-10-31T23:59:59", required = true) @RequestParam LocalDateTime endDate,
                        @PageableDefault(size = 10) Pageable pageable) {
                Page<OrderSummaryResponse> orders = orderService.getUserOrdersByDateRange(userId, startDate, endDate,
                                pageable);
                return ResponseEntity.ok(orders);
        }

        // ========== ESTADÍSTICAS (ADMIN/EMPLOYEE) ==========

        @Operation(summary = "Obtener estadísticas generales de órdenes", description = "Obtiene estadísticas generales del sistema de órdenes. Solo empleados y administradores.", security = @SecurityRequirement(name = "bearerAuth"), responses = {
                        @ApiResponse(responseCode = "200", description = "Estadísticas obtenidas correctamente", content = @Content(mediaType = "application/json", schema = @Schema(implementation = OrderStatisticsResponse.class)))
        })
        @GetMapping("/statistics")
        @PreAuthorize(ADMIN_OR_EMPLOYEE)
        public ResponseEntity<OrderStatisticsResponse> getOrderStatistics() {
                OrderStatisticsResponse statistics = orderService.getOrderStatistics();
                return ResponseEntity.ok(statistics);
        }

        @Operation(summary = "Obtener estadísticas de órdenes por rango de fechas", description = "Obtiene estadísticas de órdenes dentro de un rango de fechas específico. Solo empleados y administradores.", security = @SecurityRequirement(name = "bearerAuth"), responses = {
                        @ApiResponse(responseCode = "200", description = "Estadísticas obtenidas correctamente", content = @Content(mediaType = "application/json", schema = @Schema(implementation = OrderStatisticsResponse.class)))
        })
        @GetMapping("/statistics/date-range")
        @PreAuthorize(ADMIN_OR_EMPLOYEE)
        public ResponseEntity<OrderStatisticsResponse> getOrderStatisticsByDateRange(
                        @Parameter(description = "Fecha de inicio", example = "2024-10-01T00:00:00", required = true) @RequestParam LocalDateTime startDate,
                        @Parameter(description = "Fecha de fin", example = "2024-10-31T23:59:59", required = true) @RequestParam LocalDateTime endDate) {
                OrderStatisticsResponse statistics = orderService.getOrderStatisticsByDateRange(startDate, endDate);
                return ResponseEntity.ok(statistics);
        }

        @Operation(summary = "Obtener productos más vendidos", description = "Obtiene la lista de productos más vendidos ordenados por cantidad. Solo empleados y administradores.", security = @SecurityRequirement(name = "bearerAuth"), responses = {
                        @ApiResponse(responseCode = "200", description = "Productos más vendidos obtenidos correctamente", content = @Content(mediaType = "application/json", schema = @Schema(implementation = TopSellingItemResponse.class)))
        })
        @GetMapping("/top-selling/products")
        @PreAuthorize(ADMIN_OR_EMPLOYEE)
        public ResponseEntity<List<TopSellingItemResponse>> getTopSellingProducts() {
                List<TopSellingItemResponse> topProducts = orderService.getTopSellingProducts();
                return ResponseEntity.ok(topProducts);
        }

        @Operation(summary = "Obtener promociones más vendidas", description = "Obtiene la lista de promociones más vendidas ordenadas por cantidad. Solo empleados y administradores.", security = @SecurityRequirement(name = "bearerAuth"), responses = {
                        @ApiResponse(responseCode = "200", description = "Promociones más vendidas obtenidas correctamente", content = @Content(mediaType = "application/json", schema = @Schema(implementation = TopSellingItemResponse.class)))
        })
        @GetMapping("/top-selling/promotions")
        @PreAuthorize(ADMIN_OR_EMPLOYEE)
        public ResponseEntity<List<TopSellingItemResponse>> getTopSellingPromotions() {
                List<TopSellingItemResponse> topPromotions = orderService.getTopSellingPromotions();
                return ResponseEntity.ok(topPromotions);
        }

        // ========== VERIFICACIONES ==========

        @Operation(summary = "Verificar si el usuario tiene órdenes activas", description = "Verifica si el usuario autenticado tiene órdenes activas (pendientes o en preparación).", security = @SecurityRequirement(name = "bearerAuth"), responses = {
                        @ApiResponse(responseCode = "200", description = "Verificación realizada correctamente", content = @Content(mediaType = "application/json", examples = @ExampleObject(name = "Tiene órdenes activas", value = "true")))
        })
        @GetMapping("/has-active-orders")
        @PreAuthorize(AUTHENTICATED)
        public ResponseEntity<Boolean> hasActiveOrders(
                        @AuthenticationPrincipal User user) {
                boolean hasActiveOrders = orderService.userHasActiveOrders(user.getId());
                return ResponseEntity.ok(hasActiveOrders);
        }
}