package com.foodtruck.backend.presentation.exception.product;

public class ProductExceptions {

    public static class ProductNotFoundException extends RuntimeException {
        public ProductNotFoundException(String message) {
            super(message);
        }

        public ProductNotFoundException(Long id) {
            super("Producto con ID " + id + " no encontrado");
        }

        public ProductNotFoundException(String field, String value) {
            super("Producto con " + field + " '" + value + "' no encontrado");
        }
    }

    public static class ProductAlreadyExistsException extends RuntimeException {
        public ProductAlreadyExistsException(String productName) {
            super("Ya existe un producto con el nombre: " + productName);
        }
    }

    public static class InvalidProductPriceException extends RuntimeException {
        public InvalidProductPriceException(String message) {
            super(message);
        }

        public InvalidProductPriceException() {
            super("El precio del producto debe ser mayor a 0");
        }
    }

    public static class ProductNameTooLongException extends RuntimeException {
        public ProductNameTooLongException(String message) {
            super(message);
        }

        public ProductNameTooLongException(int maxLength) {
            super("El nombre del producto no puede exceder los " + maxLength + " caracteres");
        }
    }

    public static class EmptyProductNameException extends RuntimeException {
        public EmptyProductNameException(String message) {
            super(message);
        }

        public EmptyProductNameException() {
            super("El nombre del producto no puede estar vacío");
        }
    }

    public static class InvalidProductImageException extends RuntimeException {
        public InvalidProductImageException(String message) {
            super(message);
        }

        public InvalidProductImageException() {
            super("El archivo de imagen no es válido");
        }
    }

    public static class ProductImageTooLargeException extends RuntimeException {
        public ProductImageTooLargeException(String message) {
            super(message);
        }

        public ProductImageTooLargeException(long maxSize) {
            super("La imagen del producto no puede exceder los " + (maxSize / 1024 / 1024) + "MB");
        }
    }

    public static class ProductImageProcessingException extends RuntimeException {
        public ProductImageProcessingException(String message) {
            super(message);
        }

        public ProductImageProcessingException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public static class CategoryNotFoundForProductException extends RuntimeException {
        public CategoryNotFoundForProductException(String message) {
            super(message);
        }

        public CategoryNotFoundForProductException(Long categoryId) {
            super("Categoría con ID " + categoryId + " no encontrada para asignar al producto");
        }
    }

    public static class ProductInactiveCategoryException extends RuntimeException {
        public ProductInactiveCategoryException(String categoryName) {
            super("No se puede crear/actualizar producto en la categoría inactiva: " + categoryName);
        }
    }
}