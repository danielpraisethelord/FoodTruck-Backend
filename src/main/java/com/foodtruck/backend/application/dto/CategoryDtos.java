package com.foodtruck.backend.application.dto;

import java.math.BigDecimal;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Conjunto de DTOs utilizados para las operaciones relacionadas con categorías de productos")
public class CategoryDtos {

        @Schema(description = "Respuesta que contiene los detalles completos de una categoría")
        public record CategoryResponse(
                        @Schema(description = "Identificador único de la categoría", example = "1") Long id,

                        @Schema(description = "Nombre de la categoría", example = "Bebidas") String name,

                        @Schema(description = "Lista de productos asociados a esta categoría") List<String> products) {
        }

        @Schema(description = "Respuesta detallada que contiene la categoría con todos los detalles de sus productos")
        public record CategoryDetailedResponse(
                        @Schema(description = "Identificador único de la categoría", example = "1") Long id,

                        @Schema(description = "Nombre de la categoría", example = "Bebidas") String name,

                        @Schema(description = "Lista completa de productos asociados a esta categoría") List<ProductSummary> products) {
        }

        @Schema(description = "Resumen de un producto para mostrar en las respuestas de categoría")
        public record ProductSummary(
                        @Schema(description = "Identificador único del producto", example = "1") Long id,

                        @Schema(description = "Nombre del producto", example = "Coca Cola") String name,

                        @Schema(description = "Descripción del producto", example = "Bebida gaseosa refrescante") String description,

                        @Schema(description = "Precio del producto", example = "2.50") BigDecimal price,

                        @Schema(description = "URL de la imagen del producto", example = "http://localhost:8081/public/product/product123.jpg") String imageUrl,

                        @Schema(description = "Indica si el producto está activo", example = "true") Boolean active) {
        }

        @Schema(description = "Datos requeridos para crear una nueva categoría")
        public record CategoryCreateRequest(
                        @NotBlank(message = "El nombre de la categoría es obligatorio") @Size(max = 100, message = "El nombre de la categoría no puede exceder los 100 caracteres") @Schema(description = "Nombre único de la categoría", example = "Bebidas", maxLength = 100, requiredMode = Schema.RequiredMode.REQUIRED) String name) {
        }

        @Schema(description = "Datos para actualizar una categoría existente")
        public record CategoryUpdateRequest(
                        @NotBlank(message = "El nombre de la categoría es obligatorio") @Size(max = 100, message = "El nombre de la categoría no puede exceder los 100 caracteres") @Schema(description = "Nuevo nombre para la categoría", example = "Bebidas Frías", maxLength = 100, requiredMode = Schema.RequiredMode.REQUIRED) String name) {
        }

        @Schema(description = "Respuesta simple que contiene solo información básica de la categoría")
        public record CategorySimpleResponse(
                        @Schema(description = "Identificador único de la categoría", example = "1") Long id,

                        @Schema(description = "Nombre de la categoría", example = "Bebidas") String name) {
        }
}