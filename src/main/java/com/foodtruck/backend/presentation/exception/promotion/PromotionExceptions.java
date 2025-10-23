package com.foodtruck.backend.presentation.exception.promotion;

public class PromotionExceptions {

    /**
     * Excepción lanzada cuando no se encuentra una promoción
     */
    public static class PromotionNotFoundException extends RuntimeException {
        public PromotionNotFoundException(String message) {
            super(message);
        }

        public PromotionNotFoundException(Long promotionId) {
            super("Promoción con ID " + promotionId + " no encontrada");
        }
    }

    /**
     * Excepción lanzada cuando hay errores de validación en promociones temporales
     */
    public static class InvalidTemporaryPromotionException extends RuntimeException {
        public InvalidTemporaryPromotionException(String message) {
            super(message);
        }
    }

    /**
     * Excepción lanzada cuando hay errores de validación en promociones recurrentes
     */
    public static class InvalidRecurringPromotionException extends RuntimeException {
        public InvalidRecurringPromotionException(String message) {
            super(message);
        }
    }

    /**
     * Excepción lanzada cuando una promoción ya expiró y no puede ser activada
     */
    public static class PromotionExpiredException extends RuntimeException {
        public PromotionExpiredException(String message) {
            super(message);
        }

        public PromotionExpiredException(Long promotionId) {
            super("La promoción con ID " + promotionId + " ya ha expirado");
        }
    }

    /**
     * Excepción lanzada cuando una promoción no puede ser activada por su estado
     * actual
     */
    public static class PromotionCannotBeActivatedException extends RuntimeException {
        public PromotionCannotBeActivatedException(String message) {
            super(message);
        }
    }

    /**
     * Excepción lanzada cuando se intenta asociar productos inexistentes a una
     * promoción
     */
    public static class ProductsNotFoundForPromotionException extends RuntimeException {
        public ProductsNotFoundForPromotionException(String message) {
            super(message);
        }
    }

    /**
     * Excepción lanzada cuando una promoción no tiene productos asociados
     */
    public static class PromotionMustHaveProductsException extends RuntimeException {
        public PromotionMustHaveProductsException() {
            super("Una promoción debe tener al menos un producto asociado");
        }
    }

    /**
     * Excepción lanzada cuando las reglas semanales tienen conflictos de horarios
     */
    public static class ConflictingWeeklyRulesException extends RuntimeException {
        public ConflictingWeeklyRulesException(String message) {
            super(message);
        }
    }

    /**
     * Excepción lanzada cuando las fechas de una promoción temporal son inválidas
     */
    public static class InvalidPromotionDatesException extends RuntimeException {
        public InvalidPromotionDatesException(String message) {
            super(message);
        }
    }

    /**
     * Excepción lanzada cuando hay problemas con la subida de imagen
     */
    public static class PromotionImageUploadException extends RuntimeException {
        public PromotionImageUploadException(String message) {
            super(message);
        }

        public PromotionImageUploadException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    /**
     * Excepción lanzada cuando el tipo de promoción no coincide con los datos
     * proporcionados
     */
    public static class PromotionTypeMismatchException extends RuntimeException {
        public PromotionTypeMismatchException(String message) {
            super(message);
        }
    }

    /**
     * Excepción lanzada cuando una promoción no puede ser eliminada
     */
    public static class PromotionCannotBeDeletedException extends RuntimeException {
        public PromotionCannotBeDeletedException(String message) {
            super(message);
        }
    }
}