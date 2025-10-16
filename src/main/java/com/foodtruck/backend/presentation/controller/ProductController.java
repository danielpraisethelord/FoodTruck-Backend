package com.foodtruck.backend.presentation.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
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
import org.springframework.web.multipart.MultipartFile;

import com.foodtruck.backend.application.dto.ProductDtos.*;
import com.foodtruck.backend.application.service.ProductService;

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
@RequestMapping("/api/products")
@Validated
@Tag(name = "Products", description = "Gestión de productos del foodtruck")
public class ProductController {

    @Autowired
    private ProductService productService;

    private static final String ADMIN_OR_EMPLOYEE = "hasRole('ADMIN') or hasRole('EMPLOYEE')";

    @Operation(summary = "Obtener todos los productos activos", description = "Retorna una lista de todos los productos activos disponibles", responses = {
            @ApiResponse(responseCode = "200", description = "Lista de productos obtenida correctamente", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProductResponse.class)))
    })
    @GetMapping("/active")
    public ResponseEntity<List<ProductResponse>> getAllActiveProducts() {
        List<ProductResponse> products = productService.findAllActiveProducts();
        return ResponseEntity.ok(products);
    }

    @Operation(summary = "Obtener todos los productos", description = "Retorna una lista de todos los productos (activos e inactivos)", responses = {
            @ApiResponse(responseCode = "200", description = "Lista de productos obtenida correctamente", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProductResponse.class)))
    })
    @GetMapping("/all")
    public ResponseEntity<List<ProductResponse>> getAllProducts() {
        List<ProductResponse> products = productService.findAllProducts();
        return ResponseEntity.ok(products);
    }

    @Operation(summary = "Obtener producto por ID", description = "Busca un producto específico por su identificador único", responses = {
            @ApiResponse(responseCode = "200", description = "Producto encontrado", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProductResponse.class))),
            @ApiResponse(responseCode = "404", description = "Producto no encontrado", content = @Content(mediaType = "application/json", examples = @ExampleObject(name = "Producto no encontrado", value = """
                    {
                        "error": "PRODUCT_NOT_FOUND",
                        "message": "Producto con ID 999 no encontrado"
                    }
                    """)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProductById(
            @Parameter(description = "ID del producto", example = "1", required = true) @PathVariable @NotNull @Positive Long id) {
        ProductResponse product = productService.findProductById(id);
        return ResponseEntity.ok(product);
    }

    @Operation(summary = "Obtener producto activo por ID", description = "Busca un producto activo específico por su identificador único", responses = {
            @ApiResponse(responseCode = "200", description = "Producto activo encontrado", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProductResponse.class))),
            @ApiResponse(responseCode = "404", description = "Producto no encontrado o inactivo")
    })
    @GetMapping("/{id}/active")
    public ResponseEntity<ProductResponse> getActiveProductById(
            @Parameter(description = "ID del producto", example = "1", required = true) @PathVariable @NotNull @Positive Long id) {
        ProductResponse product = productService.findActiveProductById(id);
        return ResponseEntity.ok(product);
    }

    @Operation(summary = "Obtener productos por categoría", description = "Retorna todos los productos de una categoría específica", responses = {
            @ApiResponse(responseCode = "200", description = "Productos de la categoría obtenidos correctamente", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProductResponse.class)))
    })
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<ProductResponse>> getProductsByCategory(
            @Parameter(description = "ID de la categoría", example = "1", required = true) @PathVariable @NotNull @Positive Long categoryId) {
        List<ProductResponse> products = productService.findProductsByCategory(categoryId);
        return ResponseEntity.ok(products);
    }

    @Operation(summary = "Obtener productos activos por categoría", description = "Retorna todos los productos activos de una categoría específica", responses = {
            @ApiResponse(responseCode = "200", description = "Productos activos de la categoría obtenidos correctamente", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProductResponse.class)))
    })
    @GetMapping("/category/{categoryId}/active")
    public ResponseEntity<List<ProductResponse>> getActiveProductsByCategory(
            @Parameter(description = "ID de la categoría", example = "1", required = true) @PathVariable @NotNull @Positive Long categoryId) {
        List<ProductResponse> products = productService.findActiveProductsByCategory(categoryId);
        return ResponseEntity.ok(products);
    }

    @Operation(summary = "Buscar productos por nombre", description = "Busca productos que contengan el texto especificado en su nombre", responses = {
            @ApiResponse(responseCode = "200", description = "Búsqueda realizada correctamente", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProductSearchResponse.class)))
    })
    @GetMapping("/search")
    public ResponseEntity<List<ProductSearchResponse>> searchProducts(
            @Parameter(description = "Texto a buscar en el nombre del producto", example = "hamburguesa", required = true) @RequestParam String name) {
        List<ProductSearchResponse> products = productService.searchProductsByName(name);
        return ResponseEntity.ok(products);
    }

    @Operation(summary = "Buscar productos activos por nombre", description = "Busca productos activos que contengan el texto especificado en su nombre", responses = {
            @ApiResponse(responseCode = "200", description = "Búsqueda de productos activos realizada correctamente", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProductSearchResponse.class)))
    })
    @GetMapping("/search/active")
    public ResponseEntity<List<ProductSearchResponse>> searchActiveProducts(
            @Parameter(description = "Texto a buscar en el nombre del producto", example = "hamburguesa", required = true) @RequestParam String name) {
        List<ProductSearchResponse> products = productService.searchActiveProductsByName(name);
        return ResponseEntity.ok(products);
    }

    @Operation(summary = "Búsqueda avanzada de productos", description = "Busca productos aplicando múltiples filtros opcionales", responses = {
            @ApiResponse(responseCode = "200", description = "Búsqueda avanzada realizada correctamente", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProductSearchResponse.class)))
    })
    @GetMapping("/search/advanced")
    public ResponseEntity<List<ProductSearchResponse>> searchProductsWithFilters(
            @Parameter(description = "Texto a buscar en el nombre (opcional)", example = "hamburguesa") @RequestParam(required = false) String name,
            @Parameter(description = "ID de la categoría a filtrar (opcional)", example = "1") @RequestParam(required = false) Long categoryId,
            @Parameter(description = "Estado de activación a filtrar (opcional)", example = "true") @RequestParam(required = false) Boolean active) {
        List<ProductSearchResponse> products = productService.searchProductsWithFilters(name, categoryId, active);
        return ResponseEntity.ok(products);
    }

    @Operation(summary = "Crear un nuevo producto. Requiere rol de ADMIN o EMPLOYEE.", security = @SecurityRequirement(name = "bearerAuth"), description = "Crea un nuevo producto en el sistema", responses = {
            @ApiResponse(responseCode = "201", description = "Producto creado correctamente", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProductResponse.class))),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
            @ApiResponse(responseCode = "409", description = "Ya existe un producto con ese nombre")
    })
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize(ADMIN_OR_EMPLOYEE)
    public ResponseEntity<ProductResponse> createProduct(@Valid @ModelAttribute ProductCreateFormData formData) {
        ProductCreateRequest request = new ProductCreateRequest(
                formData.getName(),
                formData.getPrice(),
                formData.getImage(),
                formData.getActive(),
                formData.getCategoryId());

        ProductResponse product = productService.createProduct(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(product);
    }

    @Operation(summary = "Actualizar producto", description = "Actualiza los datos de un producto existente", responses = {
            @ApiResponse(responseCode = "200", description = "Producto actualizado correctamente", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProductResponse.class))),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
            @ApiResponse(responseCode = "404", description = "Producto no encontrado"),
            @ApiResponse(responseCode = "409", description = "Ya existe un producto con ese nombre")
    })
    @PutMapping("/{id}")
    @PreAuthorize(ADMIN_OR_EMPLOYEE)
    public ResponseEntity<ProductResponse> updateProduct(
            @Parameter(description = "ID del producto", example = "1", required = true) @PathVariable @NotNull @Positive Long id,
            @Valid @RequestBody ProductUpdateRequest request) {
        ProductResponse product = productService.updateProduct(id, request);
        return ResponseEntity.ok(product);
    }

    @Operation(summary = "Actualizar imagen del producto", description = "Actualiza la imagen de un producto existente", responses = {
            @ApiResponse(responseCode = "200", description = "Imagen actualizada correctamente", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProductImageUpdateResponse.class))),
            @ApiResponse(responseCode = "400", description = "Imagen inválida"),
            @ApiResponse(responseCode = "404", description = "Producto no encontrado")
    })
    @PatchMapping(value = "/{id}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize(ADMIN_OR_EMPLOYEE)
    public ResponseEntity<ProductImageUpdateResponse> updateProductImage(
            @Parameter(description = "ID del producto", example = "1", required = true) @PathVariable @NotNull @Positive Long id,
            @RequestParam("image") @NotNull MultipartFile image) {

        ProductImageUpdateRequest request = new ProductImageUpdateRequest(image);
        ProductImageUpdateResponse response = productService.updateProductImage(id, request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Cambiar estado del producto", description = "Activa o desactiva un producto", responses = {
            @ApiResponse(responseCode = "200", description = "Estado del producto cambiado correctamente", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProductResponse.class))),
            @ApiResponse(responseCode = "404", description = "Producto no encontrado")
    })
    @PatchMapping("/{id}/status")
    @PreAuthorize(ADMIN_OR_EMPLOYEE)
    public ResponseEntity<ProductResponse> toggleProductStatus(
            @Parameter(description = "ID del producto", example = "1", required = true) @PathVariable @NotNull @Positive Long id,
            @Parameter(description = "Nuevo estado del producto", example = "true", required = true) @RequestParam Boolean active) {
        ProductResponse product = productService.toggleProductStatus(id, active);
        return ResponseEntity.ok(product);
    }

    @Operation(summary = "Eliminar producto", description = "Elimina permanentemente un producto del sistema", responses = {
            @ApiResponse(responseCode = "204", description = "Producto eliminado correctamente"),
            @ApiResponse(responseCode = "404", description = "Producto no encontrado")
    })
    @DeleteMapping("/{id}")
    @PreAuthorize(ADMIN_OR_EMPLOYEE)
    public ResponseEntity<Void> deleteProduct(
            @Parameter(description = "ID del producto", example = "1", required = true) @PathVariable @NotNull @Positive Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Contar productos por categoría", description = "Retorna el número total de productos en una categoría", responses = {
            @ApiResponse(responseCode = "200", description = "Conteo realizado correctamente", content = @Content(mediaType = "application/json", examples = @ExampleObject(name = "Conteo de productos", value = "15")))
    })
    @GetMapping("/category/{categoryId}/count")
    public ResponseEntity<Long> countProductsByCategory(
            @Parameter(description = "ID de la categoría", example = "1", required = true) @PathVariable @NotNull @Positive Long categoryId) {
        long count = productService.countProductsByCategory(categoryId);
        return ResponseEntity.ok(count);
    }

    @Operation(summary = "Contar productos activos por categoría", description = "Retorna el número de productos activos en una categoría", responses = {
            @ApiResponse(responseCode = "200", description = "Conteo de productos activos realizado correctamente", content = @Content(mediaType = "application/json", examples = @ExampleObject(name = "Conteo de productos activos", value = "12")))
    })
    @GetMapping("/category/{categoryId}/count/active")
    public ResponseEntity<Long> countActiveProductsByCategory(
            @Parameter(description = "ID de la categoría", example = "1", required = true) @PathVariable @NotNull @Positive Long categoryId) {
        long count = productService.countActiveProductsByCategory(categoryId);
        return ResponseEntity.ok(count);
    }
}