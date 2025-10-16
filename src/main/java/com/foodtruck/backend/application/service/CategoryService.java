package com.foodtruck.backend.application.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.foodtruck.backend.application.dto.CategoryDtos.*;
import com.foodtruck.backend.domain.model.Category;
import com.foodtruck.backend.domain.model.Product;
import com.foodtruck.backend.domain.repository.CategoryRepository;
import com.foodtruck.backend.presentation.exception.category.CategoryExceptions.*;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    /**
     * Obtiene todas las categorías ordenadas por nombre
     * 
     * @return lista de categorías simples
     */
    public List<CategorySimpleResponse> getAllCategories() {
        List<Category> categories = categoryRepository.findAllByOrderByNameAsc();
        return categories.stream()
                .map(this::mapToCategorySimpleResponse)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene una categoría por ID
     * 
     * @param id ID de la categoría
     * @return categoría con nombres de productos
     */
    public CategoryResponse getCategoryById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException(id));
        return mapToCategoryResponse(category);
    }

    /**
     * Obtiene categorías que tienen productos
     * 
     * @return lista de categorías con productos
     */
    public List<CategorySimpleResponse> getCategoriesWithProducts() {
        List<Category> categories = categoryRepository.findCategoriesWithProducts();
        return categories.stream()
                .map(this::mapToCategorySimpleResponse)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene categorías que NO tienen productos
     * 
     * @return lista de categorías vacías
     */
    public List<CategorySimpleResponse> getCategoriesWithoutProducts() {
        List<Category> categories = categoryRepository.findCategoriesWithoutProducts();
        return categories.stream()
                .map(this::mapToCategorySimpleResponse)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene categorías que tienen productos activos
     * 
     * @return lista de categorías con productos activos
     */
    public List<CategorySimpleResponse> getCategoriesWithActiveProducts() {
        List<Category> categories = categoryRepository.findCategoriesWithActiveProducts();
        return categories.stream()
                .map(this::mapToCategorySimpleResponse)
                .collect(Collectors.toList());
    }

    /**
     * Busca categorías por nombre (búsqueda parcial)
     * 
     * @param name texto a buscar
     * @return lista de categorías que coinciden
     */
    public List<CategorySimpleResponse> searchCategoriesByName(String name) {
        List<Category> categories = categoryRepository.findByNameContainingIgnoreCase(name);
        return categories.stream()
                .map(this::mapToCategorySimpleResponse)
                .collect(Collectors.toList());
    }

    /**
     * Crea una nueva categoría
     * 
     * @param request datos para crear la categoría
     * @return categoría creada
     */
    public CategorySimpleResponse createCategory(CategoryCreateRequest request) {
        // Validar que no existe una categoría con ese nombre
        if (categoryRepository.existsByName(request.name())) {
            throw new CategoryAlreadyExistsException(request.name());
        }

        Category category = Category.builder()
                .name(request.name())
                .build();

        Category savedCategory = categoryRepository.save(category);
        return mapToCategorySimpleResponse(savedCategory);
    }

    /**
     * Actualiza una categoría existente
     * 
     * @param id      ID de la categoría a actualizar
     * @param request nuevos datos
     * @return categoría actualizada
     */
    public CategorySimpleResponse updateCategory(Long id, CategoryUpdateRequest request) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException(id));

        // Validar que no existe otra categoría con ese nombre
        if (categoryRepository.existsByNameAndIdNot(request.name(), id)) {
            throw new CategoryAlreadyExistsException(request.name());
        }

        category.setName(request.name());
        Category updatedCategory = categoryRepository.save(category);
        return mapToCategorySimpleResponse(updatedCategory);
    }

    /**
     * Elimina una categoría
     * 
     * @param id ID de la categoría a eliminar
     */
    public void deleteCategory(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException(id));

        // Verificar que no tenga productos asociados
        Long productCount = categoryRepository.countProductsByCategoryId(id);
        if (productCount > 0) {
            throw new CategoryHasProductsException(category.getName(), productCount.intValue());
        }

        categoryRepository.delete(category);
    }

    /**
     * Busca una categoría por nombre exacto
     * 
     * @param name nombre de la categoría
     * @return categoría encontrada
     */
    public CategorySimpleResponse getCategoryByName(String name) {
        Category category = categoryRepository.findByName(name)
                .orElseThrow(() -> new CategoryNotFoundException("name", name));
        return mapToCategorySimpleResponse(category);
    }

    /**
     * Cuenta los productos de una categoría
     * 
     * @param categoryId ID de la categoría
     * @return número de productos
     */
    public Long countProductsByCategory(Long categoryId) {
        // Verificar que la categoría existe
        categoryRepository.findById(categoryId)
                .orElseThrow(() -> new CategoryNotFoundException(categoryId));

        return categoryRepository.countProductsByCategoryId(categoryId);
    }

    /**
     * Obtiene una categoría con información detallada de sus productos
     * 
     * @param id ID de la categoría
     * @return categoría con productos detallados
     */
    public CategoryDetailedResponse getCategoryDetailed(Long id) {
        Category category = categoryRepository.findByIdWithProducts(id)
                .orElseThrow(() -> new CategoryNotFoundException(id));
        return mapToCategoryDetailedResponse(category);
    }

    // Métodos de mapeo
    private CategorySimpleResponse mapToCategorySimpleResponse(Category category) {
        return new CategorySimpleResponse(
                category.getId(),
                category.getName());
    }

    private CategoryResponse mapToCategoryResponse(Category category) {
        List<String> productNames = category.getProducts().stream()
                .map(product -> product.getName())
                .collect(Collectors.toList());

        return new CategoryResponse(
                category.getId(),
                category.getName(),
                productNames);
    }

    /**
     * Mapea una entidad Category a CategoryDetailedResponse
     */
    private CategoryDetailedResponse mapToCategoryDetailedResponse(Category category) {
        List<ProductSummary> productSummaries = category.getProducts().stream()
                .map(this::mapToProductSummary)
                .collect(Collectors.toList());

        return new CategoryDetailedResponse(
                category.getId(),
                category.getName(),
                productSummaries);
    }

    /**
     * Mapea una entidad Product a ProductSummary
     */
    private ProductSummary mapToProductSummary(Product product) {
        return new ProductSummary(
                product.getId(),
                product.getName(),
                product.getPrice(),
                product.getImage(),
                product.getActive());
    }
}