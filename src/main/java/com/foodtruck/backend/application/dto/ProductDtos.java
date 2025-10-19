package com.foodtruck.backend.application.dto;

import java.math.BigDecimal;

import org.springframework.web.multipart.MultipartFile;

import com.foodtruck.backend.infrastructure.validation.ValidImage;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "Conjunto de DTOs utilizados para las operaciones relacionadas con productos del food truck")
public class ProductDtos {

        @Schema(description = "Respuesta simple que contiene información básica de un producto")
        public record ProductSimpleResponse(
                        @Schema(description = "Identificador único del producto", example = "1") Long id,
                        @Schema(description = "Nombre del producto", example = "Hamburguesa Clásica") String name,
                        @Schema(description = "Precio del producto", example = "15.99") BigDecimal price,
                        @Schema(description = "Indica si el producto está activo", example = "true") Boolean active) {
        }

        @Schema(description = "Respuesta completa que contiene todos los detalles de un producto")
        public record ProductResponse(
                        @Schema(description = "Identificador único del producto", example = "1") Long id,
                        @Schema(description = "Nombre del producto", example = "Hamburguesa Clásica") String name,
                        @Schema(description = "Precio del producto", example = "15.99") BigDecimal price,
                        @Schema(description = "URL de la imagen del producto", example = "http://localhost:8081/public/images/hamburguesa-clasica.jpg") String imageUrl,
                        @Schema(description = "Indica si el producto está activo", example = "true") Boolean active,
                        @Schema(description = "Información de la categoría a la que pertenece") CategoryInfo category) {
        }

        @Schema(description = "Información básica de la categoría para incluir en respuestas de productos")
        public record CategoryInfo(
                        @Schema(description = "Identificador único de la categoría", example = "1") Long id,
                        @Schema(description = "Nombre de la categoría", example = "Hamburguesas") String name) {
        }

        @Schema(description = "Datos requeridos para crear un nuevo producto")
        public record ProductCreateRequest(
                        @NotBlank(message = "El nombre del producto es obligatorio") @Size(max = 120, message = "El nombre del producto no puede exceder los 120 caracteres") @Schema(description = "Nombre del producto", example = "Hamburguesa Clásica", maxLength = 120, requiredMode = Schema.RequiredMode.REQUIRED) String name,

                        @NotNull(message = "El precio del producto es obligatorio") @DecimalMin(value = "0.01", message = "El precio debe ser mayor a 0") @Schema(description = "Precio del producto", example = "15.99", minimum = "0.01", requiredMode = Schema.RequiredMode.REQUIRED) BigDecimal price,

                        @Schema(description = "Imagen del producto", type = "string", format = "binary", requiredMode = Schema.RequiredMode.NOT_REQUIRED) MultipartFile image,

                        @Schema(description = "Indica si el producto estará activo al crearlo", example = "true", defaultValue = "true") Boolean active,

                        @NotNull(message = "La categoría es obligatoria") @Schema(description = "ID de la categoría a la que pertenece el producto", example = "1", requiredMode = Schema.RequiredMode.REQUIRED) Long categoryId) {

                public ProductCreateRequest {
                        if (active == null) {
                                active = true;
                        }
                }
        }

        @Schema(description = "Datos para crear un producto via multipart/form-data")
        public static class ProductCreateFormData {

                @NotBlank(message = "El nombre del producto es obligatorio")
                @Size(max = 120, message = "El nombre del producto no puede exceder los 120 caracteres")
                @Schema(description = "Nombre del producto", example = "Hamburguesa Clásica", requiredMode = Schema.RequiredMode.REQUIRED)
                private String name;

                @NotNull(message = "El precio del producto es obligatorio")
                @DecimalMin(value = "0.01", message = "El precio debe ser mayor a 0")
                @Schema(description = "Precio del producto", example = "15.99", requiredMode = Schema.RequiredMode.REQUIRED)
                private BigDecimal price;

                @Schema(description = "Imagen del producto", type = "string", format = "binary", requiredMode = Schema.RequiredMode.REQUIRED, example = "archivo_imagen.jpg")
                @ValidImage(message = "La imagen del producto no es válida", required = true, maxSize = 5 * 1024
                                * 1024, allowedTypes = {
                                                "image/jpeg", "image/jpg", "image/png", "image/gif", "image/webp" })
                private MultipartFile image;

                @Schema(description = "Indica si el producto estará activo al crearlo", example = "true", defaultValue = "true")
                private Boolean active = true;

                @NotNull(message = "La categoría es obligatoria")
                @Schema(description = "ID de la categoría a la que pertenece el producto", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
                private Long categoryId;

                // Constructor vacío
                public ProductCreateFormData() {
                }

                // Getters y Setters
                public String getName() {
                        return name;
                }

                public void setName(String name) {
                        this.name = name;
                }

                public BigDecimal getPrice() {
                        return price;
                }

                public void setPrice(BigDecimal price) {
                        this.price = price;
                }

                public MultipartFile getImage() {
                        return image;
                }

                public void setImage(MultipartFile image) {
                        this.image = image;
                }

                public Boolean getActive() {
                        return active;
                }

                public void setActive(Boolean active) {
                        this.active = active;
                }

                public Long getCategoryId() {
                        return categoryId;
                }

                public void setCategoryId(Long categoryId) {
                        this.categoryId = categoryId;
                }
        }

        @Schema(description = "Datos para actualizar un producto existente (sin imagen)")
        public record ProductUpdateRequest(
                        @Size(max = 120, message = "El nombre del producto no puede exceder los 120 caracteres") @Schema(description = "Nuevo nombre del producto", example = "Hamburguesa Deluxe", maxLength = 120) String name,

                        @DecimalMin(value = "0.01", message = "El precio debe ser mayor a 0") @Schema(description = "Nuevo precio del producto", example = "18.99", minimum = "0.01") BigDecimal price,

                        @Schema(description = "Estado de activación del producto", example = "true") Boolean active,

                        @Schema(description = "ID de la nueva categoría del producto", example = "2") Long categoryId) {
        }

        @Schema(description = "Datos para actualizar solo la imagen de un producto")
        public record ProductImageUpdateRequest(

                        @ValidImage(message = "La imagen del producto no es válida", required = true, maxSize = 5 * 1024
                                        * 1024, allowedTypes = {
                                                        "image/jpeg", "image/jpg", "image/png", "image/gif",
                                                        "image/webp" }) @Schema(description = "Nueva imagen del producto", type = "string", format = "binary", requiredMode = Schema.RequiredMode.REQUIRED, example = "nueva_imagen.jpg") MultipartFile image){
        }

        @Schema(description = "Respuesta tras actualizar la imagen de un producto")
        public record ProductImageUpdateResponse(
                        @Schema(description = "Mensaje de confirmación", example = "Imagen actualizada correctamente") String message,

                        @Schema(description = "URL de la nueva imagen", example = "http://localhost:8081/public/images/hamburguesa-deluxe_12345.jpg") String imageUrl,

                        @Schema(description = "ID del producto", example = "1") Long productId,

                        @Schema(description = "Nombre del producto", example = "Hamburguesa Clásica") String productName) {
        }

        @Schema(description = "Resumen de un producto para mostrar en listados de categorías")
        public record ProductSummary(
                        @Schema(description = "Identificador único del producto", example = "1") Long id,
                        @Schema(description = "Nombre del producto", example = "Hamburguesa Clásica") String name,
                        @Schema(description = "Precio del producto", example = "15.99") BigDecimal price,
                        @Schema(description = "URL de la imagen del producto", example = "http://localhost:8081/public/images/hamburguesa-clasica.jpg") String imageUrl,
                        @Schema(description = "Indica si el producto está activo", example = "true") Boolean active) {
        }

        @Schema(description = "Respuesta para búsquedas y filtros de productos")
        public record ProductSearchResponse(
                        @Schema(description = "Identificador único del producto", example = "1") Long id,
                        @Schema(description = "Nombre del producto", example = "Hamburguesa Clásica") String name,
                        @Schema(description = "Precio del producto", example = "15.99") BigDecimal price,
                        @Schema(description = "URL de la imagen del producto", example = "http://localhost:8081/public/images/hamburguesa-clasica.jpg") String imageUrl,
                        @Schema(description = "Indica si el producto está activo", example = "true") Boolean active,
                        @Schema(description = "Nombre de la categoría", example = "Hamburguesas") String categoryName) {
        }
}