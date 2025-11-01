package com.foodtruck.backend.presentation.exception.order;

public class OrderExceptions {

    /**
     * Excepción lanzada cuando no se encuentra una orden
     */
    public static class OrderNotFoundException extends RuntimeException {
        public OrderNotFoundException(String message) {
            super(message);
        }

        public OrderNotFoundException(Long orderId) {
            super("Orden con ID " + orderId + " no encontrada");
        }
    }

    /**
     * Excepción lanzada cuando una orden está vacía
     */
    public static class EmptyOrderException extends RuntimeException {
        public EmptyOrderException() {
            super("La orden debe tener al menos un item");
        }
    }

    /**
     * Excepción lanzada cuando un item de orden es inválido
     */
    public static class InvalidOrderItemException extends RuntimeException {
        public InvalidOrderItemException(String message) {
            super(message);
        }
    }

    /**
     * Excepción lanzada cuando se intenta modificar una orden que no está en estado
     * PENDIENTE
     */
    public static class OrderCannotBeModifiedException extends RuntimeException {
        public OrderCannotBeModifiedException(String message) {
            super(message);
        }

        public OrderCannotBeModifiedException(Long orderId) {
            super("La orden con ID " + orderId + " no puede ser modificada porque ya está en proceso");
        }
    }

    /**
     * Excepción lanzada cuando el cambio de estado de la orden es inválido
     */
    public static class InvalidOrderStatusTransitionException extends RuntimeException {
        public InvalidOrderStatusTransitionException(String message) {
            super(message);
        }
    }

    /**
     * Excepción lanzada cuando una orden ya está cancelada
     */
    public static class OrderAlreadyCancelledException extends RuntimeException {
        public OrderAlreadyCancelledException(Long orderId) {
            super("La orden con ID " + orderId + " ya está cancelada");
        }
    }

    /**
     * Excepción lanzada cuando una orden ya está entregada
     */
    public static class OrderAlreadyDeliveredException extends RuntimeException {
        public OrderAlreadyDeliveredException(Long orderId) {
            super("La orden con ID " + orderId + " ya está entregada");
        }
    }

    /**
     * Excepción lanzada cuando un producto no está disponible
     */
    public static class ProductNotAvailableException extends RuntimeException {
        public ProductNotAvailableException(String message) {
            super(message);
        }

        public ProductNotAvailableException(Long productId) {
            super("El producto con ID " + productId + " no está disponible");
        }
    }

    /**
     * Excepción lanzada cuando una promoción no está disponible
     */
    public static class PromotionNotAvailableException extends RuntimeException {
        public PromotionNotAvailableException(String message) {
            super(message);
        }

        public PromotionNotAvailableException(Long promotionId) {
            super("La promoción con ID " + promotionId + " no está disponible en este momento");
        }
    }

    /**
     * Excepción lanzada cuando el tiempo estimado es inválido
     */
    public static class InvalidEstimatedTimeException extends RuntimeException {
        public InvalidEstimatedTimeException(String message) {
            super(message);
        }
    }

    /**
     * Excepción lanzada cuando la propina es inválida
     */
    public static class InvalidTipException extends RuntimeException {
        public InvalidTipException(String message) {
            super(message);
        }
    }

    /**
     * Excepción lanzada cuando un usuario intenta acceder a una orden que no le
     * pertenece
     */
    public static class OrderAccessDeniedException extends RuntimeException {
        public OrderAccessDeniedException() {
            super("No tienes permiso para acceder a esta orden");
        }

        public OrderAccessDeniedException(Long orderId) {
            super("No tienes permiso para acceder a la orden con ID " + orderId);
        }
    }
}