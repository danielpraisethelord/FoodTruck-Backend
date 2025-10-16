package com.foodtruck.backend.presentation.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.foodtruck.backend.application.dto.CategoryDtos.*;
import com.foodtruck.backend.application.service.CategoryService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
@Tag(name = "Categorías", description = "Gestión de categorías de productos del food truck")
public class CategoryController {

    private final CategoryService categoryService;
    private static final String ADMIN_OR_EMPLOYEE = "hasRole('ADMIN') or hasRole('EMPLOYEE')";

    @GetMapping
    @Operation(summary = "Obtener todas las categorías", description = "Obtiene una lista de todas las categorías ordenadas por nombre", responses = {
            @ApiResponse(responseCode = "200", description = "Lista de categorías obtenida exitosamente", content = @Content(mediaType = "application/json", schema = @Schema(implementation = CategorySimpleResponse.class), examples = @ExampleObject(name = "Lista de categorías", value = """
                    [
                        {
                            "id": 1,
                            "name": "Bebidas"
                        },
                        {
                            "id": 2,
                            "name": "Comida Rápida"
                        }
                    ]
                    """))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor", content = @Content(mediaType = "application/json", examples = @ExampleObject(name = "Error interno", value = """
                    {
                        "error": "INTERNAL_ERROR",
                        "message": "Error interno del servidor"
                    }
                    """)))
    })
    // TODO: Implementar paginación para cuando haya muchas categorías
    public ResponseEntity<List<CategorySimpleResponse>> getAllCategories() {
        List<CategorySimpleResponse> categories = categoryService.getAllCategories();
        return ResponseEntity.ok(categories);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener categoría por ID", description = "Obtiene una categoría específica con la lista de nombres de sus productos", responses = {
            @ApiResponse(responseCode = "200", description = "Categoría encontrada exitosamente", content = @Content(mediaType = "application/json", schema = @Schema(implementation = CategoryResponse.class), examples = @ExampleObject(name = "Categoría con productos", value = """
                    {
                        "id": 1,
                        "name": "Bebidas",
                        "productNames": ["Coca Cola", "Pepsi", "Agua"]
                    }
                    """))),
            @ApiResponse(responseCode = "404", description = "Categoría no encontrada", content = @Content(mediaType = "application/json", examples = @ExampleObject(name = "Categoría no encontrada", value = """
                    {
                        "error": "CATEGORY_NOT_FOUND",
                        "message": "Categoría con ID 999 no encontrada"
                    }
                    """)))
    })
    public ResponseEntity<CategoryResponse> getCategoryById(
            @Parameter(description = "ID de la categoría", example = "1") @PathVariable Long id) {
        CategoryResponse category = categoryService.getCategoryById(id);
        return ResponseEntity.ok(category);
    }

    @GetMapping("/search")
    @Operation(summary = "Buscar categorías por nombre", description = "Busca categorías que contengan el texto especificado en su nombre", responses = {
            @ApiResponse(responseCode = "200", description = "Búsqueda realizada exitosamente", content = @Content(mediaType = "application/json", schema = @Schema(implementation = CategorySimpleResponse.class), examples = @ExampleObject(name = "Resultados de búsqueda", value = """
                    [
                        {
                            "id": 1,
                            "name": "Bebidas Frías"
                        },
                        {
                            "id": 3,
                            "name": "Bebidas Calientes"
                        }
                    ]
                    """))),
            @ApiResponse(responseCode = "400", description = "Parámetro de búsqueda requerido", content = @Content(mediaType = "application/json", examples = @ExampleObject(name = "Parámetro faltante", value = """
                    {
                        "error": "BAD_REQUEST",
                        "message": "El parámetro 'name' es requerido"
                    }
                    """)))
    })
    // TODO: Implementar paginación para resultados de búsqueda de categorías
    public ResponseEntity<List<CategorySimpleResponse>> searchCategories(
            @Parameter(description = "Texto a buscar en el nombre de las categorías", example = "bebida") @RequestParam String name) {
        List<CategorySimpleResponse> categories = categoryService.searchCategoriesByName(name);
        return ResponseEntity.ok(categories);
    }

    @GetMapping("/with-products")
    @Operation(summary = "Obtener categorías con productos", description = "Obtiene solo las categorías que tienen al menos un producto asociado", responses = {
            @ApiResponse(responseCode = "200", description = "Categorías con productos obtenidas exitosamente", content = @Content(mediaType = "application/json", schema = @Schema(implementation = CategorySimpleResponse.class)))
    })
    public ResponseEntity<List<CategorySimpleResponse>> getCategoriesWithProducts() {
        List<CategorySimpleResponse> categories = categoryService.getCategoriesWithProducts();
        return ResponseEntity.ok(categories);
    }

    @GetMapping("/without-products")
    @Operation(summary = "Obtener categorías vacías", description = "Obtiene las categorías que no tienen productos asociados", responses = {
            @ApiResponse(responseCode = "200", description = "Categorías vacías obtenidas exitosamente", content = @Content(mediaType = "application/json", schema = @Schema(implementation = CategorySimpleResponse.class)))
    })
    public ResponseEntity<List<CategorySimpleResponse>> getCategoriesWithoutProducts() {
        List<CategorySimpleResponse> categories = categoryService.getCategoriesWithoutProducts();
        return ResponseEntity.ok(categories);
    }

    @GetMapping("/with-active-products")
    @Operation(summary = "Obtener categorías con productos activos", description = "Obtiene las categorías que tienen productos activos", responses = {
            @ApiResponse(responseCode = "200", description = "Categorías con productos activos obtenidas exitosamente", content = @Content(mediaType = "application/json", schema = @Schema(implementation = CategorySimpleResponse.class)))
    })
    public ResponseEntity<List<CategorySimpleResponse>> getCategoriesWithActiveProducts() {
        List<CategorySimpleResponse> categories = categoryService.getCategoriesWithActiveProducts();
        return ResponseEntity.ok(categories);
    }

    @PostMapping
    @PreAuthorize(ADMIN_OR_EMPLOYEE)
    @Operation(summary = "Crear nueva categoría", description = "Crea una nueva categoría de productos. Requiere rol de ADMIN o EMPLOYEE.", security = @SecurityRequirement(name = "bearerAuth"), responses = {
            @ApiResponse(responseCode = "201", description = "Categoría creada exitosamente", content = @Content(mediaType = "application/json", schema = @Schema(implementation = CategorySimpleResponse.class), examples = @ExampleObject(name = "Categoría creada", value = """
                    {
                        "id": 5,
                        "name": "Postres"
                    }
                    """))),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos", content = @Content(mediaType = "application/json", examples = @ExampleObject(name = "Datos inválidos", value = """
                    {
                        "error": "VALIDATION_ERROR",
                        "message": "El nombre de la categoría es obligatorio"
                    }
                    """))),
            @ApiResponse(responseCode = "403", description = "Acceso denegado - Se requiere rol ADMIN o EMPLOYEE", content = @Content(mediaType = "application/json", examples = @ExampleObject(name = "Acceso denegado", value = """
                    {
                        "error": "ACCESS_DENIED",
                        "message": "Se requiere rol ADMIN o EMPLOYEE para crear categorías"
                    }
                    """))),
            @ApiResponse(responseCode = "409", description = "Categoría ya existe", content = @Content(mediaType = "application/json", examples = @ExampleObject(name = "Categoría duplicada", value = """
                    {
                        "error": "CATEGORY_ALREADY_EXISTS",
                        "message": "Ya existe una categoría con el nombre: Bebidas"
                    }
                    """)))
    })
    public ResponseEntity<CategorySimpleResponse> createCategory(
            @Valid @RequestBody CategoryCreateRequest request) {
        CategorySimpleResponse category = categoryService.createCategory(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(category);
    }

    @PutMapping("/{id}")
    @PreAuthorize(ADMIN_OR_EMPLOYEE)
    @Operation(summary = "Actualizar categoría", description = "Actualiza una categoría existente. Requiere rol de ADMIN o EMPLOYEE.", security = @SecurityRequirement(name = "bearerAuth"), responses = {
            @ApiResponse(responseCode = "200", description = "Categoría actualizada exitosamente", content = @Content(mediaType = "application/json", schema = @Schema(implementation = CategorySimpleResponse.class), examples = @ExampleObject(name = "Categoría actualizada", value = """
                    {
                        "id": 1,
                        "name": "Bebidas Refrescantes"
                    }
                    """))),
            @ApiResponse(responseCode = "403", description = "Acceso denegado - Se requiere rol ADMIN o EMPLOYEE", content = @Content(mediaType = "application/json", examples = @ExampleObject(name = "Acceso denegado", value = """
                    {
                        "error": "ACCESS_DENIED",
                        "message": "Se requiere rol ADMIN o EMPLOYEE para actualizar categorías"
                    }
                    """))),
            @ApiResponse(responseCode = "404", description = "Categoría no encontrada", content = @Content(mediaType = "application/json", examples = @ExampleObject(name = "Categoría no encontrada", value = """
                    {
                        "error": "CATEGORY_NOT_FOUND",
                        "message": "Categoría con ID 999 no encontrada"
                    }
                    """))),
            @ApiResponse(responseCode = "409", description = "Nombre de categoría ya existe", content = @Content(mediaType = "application/json", examples = @ExampleObject(name = "Nombre duplicado", value = """
                    {
                        "error": "CATEGORY_ALREADY_EXISTS",
                        "message": "Ya existe una categoría con el nombre: Comidas"
                    }
                    """)))
    })
    public ResponseEntity<CategorySimpleResponse> updateCategory(
            @Parameter(description = "ID de la categoría a actualizar", example = "1") @PathVariable Long id,
            @Valid @RequestBody CategoryUpdateRequest request) {
        CategorySimpleResponse category = categoryService.updateCategory(id, request);
        return ResponseEntity.ok(category);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize(ADMIN_OR_EMPLOYEE)
    @Operation(summary = "Eliminar categoría", description = "Elimina una categoría. Solo se puede eliminar si no tiene productos asociados. Requiere rol de ADMIN o EMPLOYEE.", security = @SecurityRequirement(name = "bearerAuth"), responses = {
            @ApiResponse(responseCode = "204", description = "Categoría eliminada exitosamente"),
            @ApiResponse(responseCode = "403", description = "Acceso denegado - Se requiere rol ADMIN o EMPLOYEE", content = @Content(mediaType = "application/json", examples = @ExampleObject(name = "Acceso denegado", value = """
                    {
                        "error": "ACCESS_DENIED",
                        "message": "Se requiere rol ADMIN o EMPLOYEE para eliminar categorías"
                    }
                    """))),
            @ApiResponse(responseCode = "404", description = "Categoría no encontrada", content = @Content(mediaType = "application/json", examples = @ExampleObject(name = "Categoría no encontrada", value = """
                    {
                        "error": "CATEGORY_NOT_FOUND",
                        "message": "Categoría con ID 999 no encontrada"
                    }
                    """))),
            @ApiResponse(responseCode = "409", description = "Categoría tiene productos asociados", content = @Content(mediaType = "application/json", examples = @ExampleObject(name = "Categoría con productos", value = """
                    {
                        "error": "CATEGORY_HAS_PRODUCTS",
                        "message": "No se puede eliminar la categoría 'Bebidas' porque tiene 5 producto(s) asociado(s)"
                    }
                    """)))
    })
    public ResponseEntity<Void> deleteCategory(
            @Parameter(description = "ID de la categoría a eliminar", example = "1") @PathVariable Long id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/name/{name}")
    @Operation(summary = "Buscar categoría por nombre exacto", description = "Busca una categoría por su nombre exacto", responses = {
            @ApiResponse(responseCode = "200", description = "Categoría encontrada", content = @Content(mediaType = "application/json", schema = @Schema(implementation = CategorySimpleResponse.class))),
            @ApiResponse(responseCode = "404", description = "Categoría no encontrada", content = @Content(mediaType = "application/json", examples = @ExampleObject(name = "Categoría no encontrada", value = """
                    {
                        "error": "CATEGORY_NOT_FOUND",
                        "message": "Categoría con name 'Inexistente' no encontrada"
                    }
                    """)))
    })
    public ResponseEntity<CategorySimpleResponse> getCategoryByName(
            @Parameter(description = "Nombre exacto de la categoría", example = "Bebidas") @PathVariable String name) {
        CategorySimpleResponse category = categoryService.getCategoryByName(name);
        return ResponseEntity.ok(category);
    }

    @GetMapping("/{id}/products/count")
    @Operation(summary = "Contar productos de una categoría", description = "Obtiene el número de productos asociados a una categoría", responses = {
            @ApiResponse(responseCode = "200", description = "Conteo obtenido exitosamente", content = @Content(mediaType = "application/json", examples = @ExampleObject(name = "Conteo de productos", value = "5"))),
            @ApiResponse(responseCode = "404", description = "Categoría no encontrada", content = @Content(mediaType = "application/json", examples = @ExampleObject(name = "Categoría no encontrada", value = """
                    {
                        "error": "CATEGORY_NOT_FOUND",
                        "message": "Categoría con ID 999 no encontrada"
                    }
                    """)))
    })
    public ResponseEntity<Long> countProductsByCategory(
            @Parameter(description = "ID de la categoría", example = "1") @PathVariable Long id) {
        Long count = categoryService.countProductsByCategory(id);
        return ResponseEntity.ok(count);
    }

    @GetMapping("/{id}/detailed")
    @Operation(summary = "Obtener categoría con productos detallados", description = "Obtiene una categoría específica con información completa de todos sus productos", responses = {
            @ApiResponse(responseCode = "200", description = "Categoría con productos detallados obtenida exitosamente", content = @Content(mediaType = "application/json", schema = @Schema(implementation = CategoryDetailedResponse.class), examples = @ExampleObject(name = "Categoría con productos detallados", value = """
                    {
                        "id": 1,
                        "name": "Bebidas",
                        "products": [
                            {
                                "id": 1,
                                "name": "Coca Cola",
                                "description": null,
                                "price": 2.50,
                                "imageUrl": "http://localhost:8081/public/images/coca-cola.jpg",
                                "active": true
                            },
                            {
                                "id": 2,
                                "name": "Pepsi",
                                "description": null,
                                "price": 2.30,
                                "imageUrl": "http://localhost:8081/public/images/pepsi.jpg",
                                "active": true
                            }
                        ]
                    }
                    """))),
            @ApiResponse(responseCode = "404", description = "Categoría no encontrada", content = @Content(mediaType = "application/json", examples = @ExampleObject(name = "Categoría no encontrada", value = """
                    {
                        "error": "CATEGORY_NOT_FOUND",
                        "message": "Categoría con ID 999 no encontrada"
                    }
                    """)))
    })
    public ResponseEntity<CategoryDetailedResponse> getCategoryDetailed(
            @Parameter(description = "ID de la categoría", example = "1") @PathVariable Long id) {
        CategoryDetailedResponse category = categoryService.getCategoryDetailed(id);
        return ResponseEntity.ok(category);
    }
}