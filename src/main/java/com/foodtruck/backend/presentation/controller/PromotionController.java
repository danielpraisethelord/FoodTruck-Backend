package com.foodtruck.backend.presentation.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.foodtruck.backend.application.dto.PromotionDtos.*;
import com.foodtruck.backend.application.service.PromotionService;
import com.foodtruck.backend.domain.types.PromotionType;

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
@RequestMapping("/api/promotions")
@Validated
@Tag(name = "Promotions", description = "Gestión de promociones del foodtruck")
public class PromotionController {

        @Autowired
        private PromotionService promotionService;

        private static final String ADMIN_OR_EMPLOYEE = "hasRole('ADMIN') or hasRole('EMPLOYEE')";

        // ========== OPERACIONES CRUD ==========

        @Operation(summary = "Crear una nueva promoción", description = "Crea una nueva promoción temporal o recurrente. Las promociones temporales pueden tener fechas de inicio y fin, mientras que las recurrentes requieren reglas semanales.", security = @SecurityRequirement(name = "bearerAuth"), responses = {
                        @ApiResponse(responseCode = "201", description = "Promoción creada correctamente", content = @Content(mediaType = "application/json", schema = @Schema(implementation = PromotionDetailResponse.class))),
                        @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos", content = @Content(mediaType = "application/json", examples = @ExampleObject(name = "Validación fallida", value = """
                                        {
                                            "error": "VALIDATION_FAILED",
                                            "message": "Las promociones recurrentes deben tener al menos una regla semanal"
                                        }
                                        """))),
                        @ApiResponse(responseCode = "404", description = "Producto no encontrado", content = @Content(mediaType = "application/json", examples = @ExampleObject(name = "Producto no encontrado", value = """
                                        {
                                            "error": "PRODUCTS_NOT_FOUND",
                                            "message": "Productos no encontrados: [999]"
                                        }
                                        """)))
        })
        @PostMapping
        @PreAuthorize(ADMIN_OR_EMPLOYEE)
        public ResponseEntity<PromotionDetailResponse> createPromotion(
                        @Valid @RequestBody CreatePromotionRequest request) {
                PromotionDetailResponse promotion = promotionService.createPromotion(request);
                return ResponseEntity.status(HttpStatus.CREATED).body(promotion);
        }

        @Operation(summary = "Actualizar una promoción existente", description = "Actualiza los datos de una promoción existente. Solo se actualizan los campos proporcionados.", security = @SecurityRequirement(name = "bearerAuth"), responses = {
                        @ApiResponse(responseCode = "200", description = "Promoción actualizada correctamente", content = @Content(mediaType = "application/json", schema = @Schema(implementation = PromotionDetailResponse.class))),
                        @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
                        @ApiResponse(responseCode = "404", description = "Promoción no encontrada"),
                        @ApiResponse(responseCode = "409", description = "Conflicto con reglas de negocio")
        })
        @PutMapping("/{id}")
        @PreAuthorize(ADMIN_OR_EMPLOYEE)
        public ResponseEntity<PromotionDetailResponse> updatePromotion(
                        @Parameter(description = "ID de la promoción", example = "1", required = true) @PathVariable @NotNull @Positive Long id,
                        @Valid @RequestBody UpdatePromotionRequest request) {
                PromotionDetailResponse promotion = promotionService.updatePromotion(id, request);
                return ResponseEntity.ok(promotion);
        }

        @Operation(summary = "Subir imagen de promoción", description = "Sube o actualiza la imagen de una promoción existente", security = @SecurityRequirement(name = "bearerAuth"), requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE, schema = @Schema(implementation = PromotionImageRequest.class))), responses = {
                        @ApiResponse(responseCode = "200", description = "Imagen subida correctamente", content = @Content(mediaType = "application/json", schema = @Schema(implementation = PromotionDetailResponse.class))),
                        @ApiResponse(responseCode = "400", description = "Imagen inválida / Datos de entrada incorrectos", content = @Content(mediaType = "application/json", examples = @ExampleObject(name = "Imagen inválida", value = """
                                        {
                                          "error": "INVALID_IMAGE",
                                          "message": "La imagen no cumple con los requisitos: tamaño máximo 5MB y tipos permitidos (jpg,png,webp)"
                                        }
                                        """))),
                        @ApiResponse(responseCode = "404", description = "Promoción no encontrada", content = @Content(mediaType = "application/json", examples = @ExampleObject(name = "Promoción no encontrada", value = """
                                        {
                                          "error": "PROMOTION_NOT_FOUND",
                                          "message": "Promoción con id 123 no encontrada"
                                        }
                                        """)))
        })
        @PatchMapping(value = "/{id}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
        @PreAuthorize(ADMIN_OR_EMPLOYEE)
        public ResponseEntity<PromotionDetailResponse> uploadPromotionImage(
                        @Parameter(description = "ID de la promoción", example = "1", required = true) @PathVariable @NotNull @Positive Long id,
                        @Valid @ModelAttribute PromotionImageRequest request) {
                PromotionDetailResponse promotion = promotionService.uploadPromotionImage(id, request.imageFile());
                return ResponseEntity.ok(promotion);
        }

        @Operation(summary = "Cambiar estado de la promoción", description = "Activa o desactiva una promoción", security = @SecurityRequirement(name = "bearerAuth"), responses = {
                        @ApiResponse(responseCode = "200", description = "Estado cambiado correctamente", content = @Content(mediaType = "application/json", schema = @Schema(implementation = PromotionDetailResponse.class))),
                        @ApiResponse(responseCode = "400", description = "No se puede activar promoción expirada", content = @Content(mediaType = "application/json", examples = @ExampleObject(name = "Promoción expirada", value = "{\"error\":\"PROMOTION_EXPIRED\",\"message\":\"La promoción con id 123 ya expiró. Actualiza las fechas para reactivarla.\"}"))),
                        @ApiResponse(responseCode = "404", description = "Promoción no encontrada", content = @Content(mediaType = "application/json", examples = @ExampleObject(name = "Promoción no encontrada", value = "{\"error\":\"PROMOTION_NOT_FOUND\",\"message\":\"Promoción con id 123 no encontrada.\"}")))
        })
        @PatchMapping("/{id}/status")
        @PreAuthorize(ADMIN_OR_EMPLOYEE)
        public ResponseEntity<PromotionDetailResponse> togglePromotionStatus(
                        @Parameter(description = "ID de la promoción", example = "1", required = true) @PathVariable @NotNull @Positive Long id) {
                PromotionDetailResponse promotion = promotionService.togglePromotionStatus(id);
                return ResponseEntity.ok(promotion);
        }

        @Operation(summary = "Eliminar promoción", description = "Elimina permanentemente una promoción del sistema. Solo se pueden eliminar promociones inactivas.", security = @SecurityRequirement(name = "bearerAuth"), responses = {
                        @ApiResponse(responseCode = "204", description = "Promoción eliminada correctamente"),
                        @ApiResponse(responseCode = "404", description = "Promoción no encontrada"),
                        @ApiResponse(responseCode = "409", description = "No se puede eliminar promoción activa")
        })
        @DeleteMapping("/{id}")
        @PreAuthorize(ADMIN_OR_EMPLOYEE)
        public ResponseEntity<Void> deletePromotion(
                        @Parameter(description = "ID de la promoción", example = "1", required = true) @PathVariable @NotNull @Positive Long id) {
                promotionService.deletePromotion(id);
                return ResponseEntity.noContent().build();
        }

        // ========== CONSULTAS BÁSICAS ==========

        @Operation(summary = "Obtener promoción por ID", description = "Obtiene una promoción específica con todos sus detalles", responses = {
                        @ApiResponse(responseCode = "200", description = "Promoción encontrada", content = @Content(mediaType = "application/json", schema = @Schema(implementation = PromotionDetailResponse.class))),
                        @ApiResponse(responseCode = "404", description = "Promoción no encontrada")
        })
        @GetMapping("/{id}")
        public ResponseEntity<PromotionDetailResponse> getPromotionById(
                        @Parameter(description = "ID de la promoción", example = "1", required = true) @PathVariable @NotNull @Positive Long id) {
                PromotionDetailResponse promotion = promotionService.getPromotionById(id);
                return ResponseEntity.ok(promotion);
        }

        @Operation(summary = "Obtener todas las promociones activas", description = "Retorna una lista de todas las promociones activas disponibles", responses = {
                        @ApiResponse(responseCode = "200", description = "Lista de promociones activas obtenida correctamente", content = @Content(mediaType = "application/json", schema = @Schema(implementation = PromotionSummaryResponse.class)))
        })
        @GetMapping("/active")
        public ResponseEntity<List<PromotionSummaryResponse>> getActivePromotions() {
                List<PromotionSummaryResponse> promotions = promotionService.getActivePromotions();
                return ResponseEntity.ok(promotions);
        }

        @Operation(summary = "Obtener todas las promociones con paginación", description = "Retorna una página de promociones ordenadas por fecha de creación descendente", responses = {
                        @ApiResponse(responseCode = "200", description = "Página de promociones obtenida correctamente")
        })
        @GetMapping
        public ResponseEntity<Page<PromotionSummaryResponse>> getPromotions(
                        @PageableDefault(size = 10) Pageable pageable) {
                Page<PromotionSummaryResponse> promotions = promotionService.getPromotions(pageable);
                return ResponseEntity.ok(promotions);
        }

        @Operation(summary = "Obtener promociones válidas actualmente", description = "Retorna promociones que están válidas en la fecha y hora actual", responses = {
                        @ApiResponse(responseCode = "200", description = "Promociones válidas obtenidas correctamente", content = @Content(mediaType = "application/json", schema = @Schema(implementation = PromotionSummaryResponse.class)))
        })
        @GetMapping("/current")
        public ResponseEntity<List<PromotionSummaryResponse>> getCurrentlyValidPromotions() {
                List<PromotionSummaryResponse> promotions = promotionService.getCurrentlyValidPromotions();
                return ResponseEntity.ok(promotions);
        }

        @Operation(summary = "Obtener promociones por tipo", description = "Retorna promociones filtradas por tipo (TEMPORARY o RECURRING)", responses = {
                        @ApiResponse(responseCode = "200", description = "Promociones por tipo obtenidas correctamente", content = @Content(mediaType = "application/json", schema = @Schema(implementation = PromotionSummaryResponse.class)))
        })
        @GetMapping("/type/{type}")
        public ResponseEntity<List<PromotionSummaryResponse>> getPromotionsByType(
                        @Parameter(description = "Tipo de promoción", example = "TEMPORARY", required = true) @PathVariable PromotionType type) {
                List<PromotionSummaryResponse> promotions = promotionService.getPromotionsByType(type);
                return ResponseEntity.ok(promotions);
        }

        // ========== BÚSQUEDAS ==========

        @Operation(summary = "Buscar promociones por nombre", description = "Busca promociones activas que contengan el texto especificado en su nombre", responses = {
                        @ApiResponse(responseCode = "200", description = "Búsqueda realizada correctamente", content = @Content(mediaType = "application/json", schema = @Schema(implementation = PromotionSummaryResponse.class)))
        })
        @GetMapping("/search")
        public ResponseEntity<List<PromotionSummaryResponse>> searchPromotionsByName(
                        @Parameter(description = "Texto a buscar en el nombre de la promoción", example = "2x1", required = true) @RequestParam String name) {
                List<PromotionSummaryResponse> promotions = promotionService.searchPromotionsByName(name);
                return ResponseEntity.ok(promotions);
        }

        // ========== CONSULTAS POR FECHAS ==========

        @Operation(summary = "Obtener promociones por fecha de inicio", description = "Retorna promociones que inicien en una fecha específica", responses = {
                        @ApiResponse(responseCode = "200", description = "Promociones por fecha de inicio obtenidas correctamente")
        })
        @GetMapping("/start-date/{date}")
        public ResponseEntity<List<PromotionSummaryResponse>> getPromotionsByStartDate(
                        @Parameter(description = "Fecha de inicio", example = "2024-10-20", required = true) @PathVariable LocalDate date) {
                List<PromotionSummaryResponse> promotions = promotionService.getPromotionsByStartDate(date);
                return ResponseEntity.ok(promotions);
        }

        @Operation(summary = "Obtener promociones por fecha de fin", description = "Retorna promociones que terminen en una fecha específica", responses = {
                        @ApiResponse(responseCode = "200", description = "Promociones por fecha de fin obtenidas correctamente")
        })
        @GetMapping("/end-date/{date}")
        public ResponseEntity<List<PromotionSummaryResponse>> getPromotionsByEndDate(
                        @Parameter(description = "Fecha de fin", example = "2024-10-25", required = true) @PathVariable LocalDate date) {
                List<PromotionSummaryResponse> promotions = promotionService.getPromotionsByEndDate(date);
                return ResponseEntity.ok(promotions);
        }

        @Operation(summary = "Obtener promociones temporales por rango de fechas", description = "Retorna promociones temporales que se encuentren dentro del rango de fechas especificado", responses = {
                        @ApiResponse(responseCode = "200", description = "Promociones temporales por rango obtenidas correctamente")
        })
        @GetMapping("/date-range")
        public ResponseEntity<List<PromotionSummaryResponse>> getTemporaryPromotionsByDateRange(
                        @Parameter(description = "Fecha de inicio del rango", example = "2024-10-01", required = true) @RequestParam LocalDate startDate,
                        @Parameter(description = "Fecha de fin del rango", example = "2024-10-31", required = true) @RequestParam LocalDate endDate) {
                List<PromotionSummaryResponse> promotions = promotionService.getTemporaryPromotionsByDateRange(
                                startDate,
                                endDate);
                return ResponseEntity.ok(promotions);
        }

        @Operation(summary = "Obtener promociones expiradas", description = "Retorna promociones temporales que ya han expirado", responses = {
                        @ApiResponse(responseCode = "200", description = "Promociones expiradas obtenidas correctamente")
        })
        @GetMapping("/expired")
        public ResponseEntity<List<PromotionSummaryResponse>> getExpiredPromotions() {
                List<PromotionSummaryResponse> promotions = promotionService.getExpiredPromotions();
                return ResponseEntity.ok(promotions);
        }

        @Operation(summary = "Obtener promociones que expiran pronto", description = "Retorna promociones que expiran dentro de un rango de fechas", responses = {
                        @ApiResponse(responseCode = "200", description = "Promociones que expiran pronto obtenidas correctamente")
        })
        @GetMapping("/expiring-soon")
        public ResponseEntity<List<PromotionSummaryResponse>> getPromotionsExpiringSoon(
                        @Parameter(description = "Fecha de inicio del rango", example = "2024-10-20", required = true) @RequestParam LocalDate startDate,
                        @Parameter(description = "Fecha de fin del rango", example = "2024-10-25", required = true) @RequestParam LocalDate endDate) {
                List<PromotionSummaryResponse> promotions = promotionService.getPromotionsExpiringBetween(startDate,
                                endDate);
                return ResponseEntity.ok(promotions);
        }

        // ========== CONSULTAS POR PRODUCTOS ==========

        @Operation(summary = "Obtener promociones activas por producto", description = "Retorna promociones activas que incluyan un producto específico", responses = {
                        @ApiResponse(responseCode = "200", description = "Promociones por producto obtenidas correctamente")
        })
        @GetMapping("/product/{productId}")
        public ResponseEntity<List<PromotionSummaryResponse>> getActivePromotionsByProduct(
                        @Parameter(description = "ID del producto", example = "1", required = true) @PathVariable @NotNull @Positive Long productId) {
                List<PromotionSummaryResponse> promotions = promotionService.getActivePromotionsByProduct(productId);
                return ResponseEntity.ok(promotions);
        }

        @Operation(summary = "Obtener promociones activas por múltiples productos", description = "Retorna promociones activas que incluyan cualquiera de los productos especificados", responses = {
                        @ApiResponse(responseCode = "200", description = "Promociones por productos obtenidas correctamente")
        })
        @GetMapping("/products")
        public ResponseEntity<List<PromotionSummaryResponse>> getActivePromotionsByProducts(
                        @Parameter(description = "IDs de productos separados por coma", example = "1,2,3", required = true) @RequestParam List<Long> productIds) {
                List<PromotionSummaryResponse> promotions = promotionService.getActivePromotionsByProducts(productIds);
                return ResponseEntity.ok(promotions);
        }

        // ========== CONSULTAS ORDENADAS POR PRECIO ==========

        @Operation(summary = "Obtener promociones activas ordenadas por precio ascendente", description = "Retorna promociones activas ordenadas de menor a mayor precio", responses = {
                        @ApiResponse(responseCode = "200", description = "Promociones ordenadas por precio obtenidas correctamente")
        })
        @GetMapping("/active/price-asc")
        public ResponseEntity<List<PromotionSummaryResponse>> getActivePromotionsByPriceAsc() {
                List<PromotionSummaryResponse> promotions = promotionService.getActivePromotionsByPriceAsc();
                return ResponseEntity.ok(promotions);
        }

        @Operation(summary = "Obtener promociones activas ordenadas por precio descendente", description = "Retorna promociones activas ordenadas de mayor a menor precio", responses = {
                        @ApiResponse(responseCode = "200", description = "Promociones ordenadas por precio obtenidas correctamente")
        })
        @GetMapping("/active/price-desc")
        public ResponseEntity<List<PromotionSummaryResponse>> getActivePromotionsByPriceDesc() {
                List<PromotionSummaryResponse> promotions = promotionService.getActivePromotionsByPriceDesc();
                return ResponseEntity.ok(promotions);
        }

        // ========== ESTADÍSTICAS ==========

        @Operation(summary = "Contar promociones por tipo", description = "Retorna el número total de promociones de un tipo específico", responses = {
                        @ApiResponse(responseCode = "200", description = "Conteo realizado correctamente", content = @Content(mediaType = "application/json", examples = @ExampleObject(name = "Conteo de promociones", value = "15")))
        })
        @GetMapping("/count/type/{type}")
        public ResponseEntity<Long> getPromotionsCountByType(
                        @Parameter(description = "Tipo de promoción", example = "TEMPORARY", required = true) @PathVariable PromotionType type) {
                Long count = promotionService.getPromotionsCountByType(type);
                return ResponseEntity.ok(count);
        }

        @Operation(summary = "Contar promociones activas por tipo", description = "Retorna el número de promociones activas de un tipo específico", responses = {
                        @ApiResponse(responseCode = "200", description = "Conteo de promociones activas realizado correctamente", content = @Content(mediaType = "application/json", examples = @ExampleObject(name = "Conteo de promociones activas", value = "12")))
        })
        @GetMapping("/count/active/type/{type}")
        public ResponseEntity<Long> getActivePromotionsCountByType(
                        @Parameter(description = "Tipo de promoción", example = "RECURRING", required = true) @PathVariable PromotionType type) {
                Long count = promotionService.getActivePromotionsCountByType(type);
                return ResponseEntity.ok(count);
        }
}