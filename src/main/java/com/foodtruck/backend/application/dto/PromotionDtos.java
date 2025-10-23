package com.foodtruck.backend.application.dto;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Set;

import org.springframework.web.multipart.MultipartFile;

import com.foodtruck.backend.domain.types.PromotionType;
import com.foodtruck.backend.infrastructure.validation.PromotionDatesValidation;
import com.foodtruck.backend.infrastructure.validation.ValidImage;
import com.foodtruck.backend.infrastructure.validation.ValidPromotionDates;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class PromotionDtos {

        @Schema(description = "DTO para crear una nueva promoción")
        @ValidPromotionDates
        public record CreatePromotionRequest(
                        @Schema(description = "Nombre de la promoción", example = "2x1 en Tacos") @NotBlank(message = "El nombre es obligatorio") @Size(max = 120, message = "El nombre no puede exceder 120 caracteres") String name,

                        @Schema(description = "Descripción detallada de la promoción", example = "Compra un taco y llévate otro gratis") String description,

                        @Schema(description = "Precio total de la promoción", example = "99.99") @NotNull(message = "El precio es obligatorio") @DecimalMin(value = "0.01", message = "El precio debe ser mayor a 0") BigDecimal price,

                        @Schema(description = "Tipo de promoción. TEMPORARY: promoción con fechas específicas de inicio y fin. RECURRING: promoción recurrente con horarios semanales", example = "TEMPORARY", allowableValues = {
                                        "TEMPORARY",
                                        "RECURRING" }) @NotNull(message = "El tipo de promoción es obligatorio") PromotionType type,

                        @Schema(description = "Fecha de inicio (solo para promociones temporales)", example = "2024-10-20") LocalDate startsAt,

                        @Schema(description = "Fecha de fin (solo para promociones temporales)", example = "2024-10-25") LocalDate endsAt,

                        @Schema(description = "IDs de los productos incluidos en la promoción") @NotEmpty(message = "La promoción debe incluir al menos un producto") Set<Long> productIds,

                        @Schema(description = "Reglas semanales (solo para promociones recurrentes)") @Valid Set<CreateWeeklyRuleRequest> weeklyRules)
                        implements PromotionDatesValidation{
        }

        @Schema(description = "DTO para crear una regla semanal")
        public record CreateWeeklyRuleRequest(
                        @Schema(description = "Día de la semana", example = "TUESDAY", allowableValues = {
                                        "MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY",
                                        "SUNDAY" }) @NotNull(message = "El día de la semana es obligatorio") DayOfWeek dayOfWeek,

                        @Schema(description = "Hora de inicio", example = "14:00:00") @NotNull(message = "La hora de inicio es obligatoria") LocalTime startTime,

                        @Schema(description = "Hora de fin", example = "18:00:00") @NotNull(message = "La hora de fin es obligatoria") LocalTime endTime){
        }

        @Schema(description = "DTO para actualizar una promoción existente")
        @ValidPromotionDates
        public record UpdatePromotionRequest(
                        @Schema(description = "Nombre de la promoción", example = "3x2 en Tacos") @Size(max = 120, message = "El nombre no puede exceder 120 caracteres") String name,

                        @Schema(description = "Descripción detallada de la promoción") String description,

                        @Schema(description = "Precio total de la promoción", example = "149.99") @DecimalMin(value = "0.01", message = "El precio debe ser mayor a 0") BigDecimal price,

                        @Schema(description = "Fecha de inicio (solo para promociones temporales)") LocalDate startsAt,

                        @Schema(description = "Fecha de fin (solo para promociones temporales)") LocalDate endsAt,

                        @Schema(description = "Estado activo de la promoción") Boolean active,

                        @Schema(description = "IDs de los productos incluidos en la promoción") Set<Long> productIds,

                        @Schema(description = "Reglas semanales (solo para promociones recurrentes)") @Valid Set<CreateWeeklyRuleRequest> weeklyRules)
                        implements PromotionDatesValidation {
                @Override
                public PromotionType type() {
                        // En la actualización no se puede cambiar el tipo de promoción
                        return null;
                }
        }

        @Schema(description = "DTO para subir imagen de promoción")
        public record PromotionImageRequest(
                        @ValidImage(message = "La imagen de la promoción no es válida", required = true, maxSize = 5
                                        * 1024 * 1024, allowedTypes = {
                                                        "image/jpeg", "image/jpg", "image/png", "image/gif",
                                                        "image/webp" }) @Schema(description = "Archivo de la imagen de la promoción", type = "string", format = "binary") MultipartFile imageFile){
        }

        @Schema(description = "DTO con información básica de una promoción")
        public record PromotionSummaryResponse(
                        @Schema(description = "ID único de la promoción", example = "1") Long id,

                        @Schema(description = "Nombre de la promoción", example = "2x1 en Tacos") String name,

                        @Schema(description = "URL de la imagen de la promoción") String imageUrl,

                        @Schema(description = "Precio total de la promoción", example = "99.99") BigDecimal price,

                        @Schema(description = "Tipo de promoción") PromotionType type,

                        @Schema(description = "Estado activo de la promoción") boolean active,

                        @Schema(description = "Indica si la promoción está actualmente válida") boolean currentlyValid) {
        }

        @Schema(description = "DTO con información completa de una promoción")
        public record PromotionDetailResponse(
                        @Schema(description = "ID único de la promoción", example = "1") Long id,

                        @Schema(description = "Nombre de la promoción", example = "2x1 en Tacos") String name,

                        @Schema(description = "Descripción detallada de la promoción") String description,

                        @Schema(description = "URL de la imagen de la promoción") String imageUrl,

                        @Schema(description = "Precio total de la promoción", example = "99.99") BigDecimal price,

                        @Schema(description = "Tipo de promoción") PromotionType type,

                        @Schema(description = "Fecha de inicio (solo promociones temporales)") LocalDate startsAt,

                        @Schema(description = "Fecha de fin (solo promociones temporales)") LocalDate endsAt,

                        @Schema(description = "Estado activo de la promoción") boolean active,

                        @Schema(description = "Indica si la promoción está actualmente válida") boolean currentlyValid,

                        @Schema(description = "Productos incluidos en la promoción") Set<ProductSummaryResponse> products,

                        @Schema(description = "Reglas semanales (solo promociones recurrentes)") Set<WeeklyRuleResponse> weeklyRules) {
        }

        @Schema(description = "DTO con información de una regla semanal")
        public record WeeklyRuleResponse(
                        @Schema(description = "ID único de la regla", example = "1") Long id,

                        @Schema(description = "Día de la semana", example = "TUESDAY") DayOfWeek dayOfWeek,

                        @Schema(description = "Hora de inicio", example = "14:00:00") LocalTime startTime,

                        @Schema(description = "Hora de fin", example = "18:00:00") LocalTime endTime,

                        @Schema(description = "Indica si la regla está activa en este momento") boolean activeNow) {
        }

        @Schema(description = "DTO con información básica de un producto")
        public record ProductSummaryResponse(
                        @Schema(description = "ID único del producto", example = "1") Long id,

                        @Schema(description = "Nombre del producto", example = "Taco de Pastor") String name,

                        @Schema(description = "URL de la imagen del producto") String imageUrl,

                        @Schema(description = "Precio individual del producto", example = "25.00") BigDecimal price) {
        }
}