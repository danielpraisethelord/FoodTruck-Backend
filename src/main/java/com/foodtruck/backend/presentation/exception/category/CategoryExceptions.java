package com.foodtruck.backend.presentation.exception.category;

public class CategoryExceptions {

    public static class CategoryNotFoundException extends RuntimeException {
        public CategoryNotFoundException(String message) {
            super(message);
        }

        public CategoryNotFoundException(Long id) {
            super("Categoría con ID " + id + " no encontrada");
        }

        public CategoryNotFoundException(String field, String value) {
            super("Categoría con " + field + " '" + value + "' no encontrada");
        }
    }

    public static class CategoryAlreadyExistsException extends RuntimeException {
        public CategoryAlreadyExistsException(String categoryName) {
            super("Ya existe una categoría con el nombre: " + categoryName);
        }
    }

    public static class CategoryHasProductsException extends RuntimeException {
        public CategoryHasProductsException(String message) {
            super(message);
        }

        public CategoryHasProductsException(String categoryName, int productCount) {
            super("No se puede eliminar la categoría '" + categoryName +
                    "' porque tiene " + productCount + " producto(s) asociado(s)");
        }
    }

    public static class CategoryNameTooLongException extends RuntimeException {
        public CategoryNameTooLongException(String message) {
            super(message);
        }

        public CategoryNameTooLongException(int maxLength) {
            super("El nombre de la categoría no puede exceder los " + maxLength + " caracteres");
        }
    }

    public static class EmptyCategoryNameException extends RuntimeException {
        public EmptyCategoryNameException(String message) {
            super(message);
        }

        public EmptyCategoryNameException() {
            super("El nombre de la categoría no puede estar vacío");
        }
    }
}