package com.foodtruck.backend.application.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.foodtruck.backend.application.dto.ProductDtos.*;
import com.foodtruck.backend.domain.model.Category;
import com.foodtruck.backend.domain.model.Product;
import com.foodtruck.backend.domain.repository.CategoryRepository;
import com.foodtruck.backend.domain.repository.ProductRepository;
import com.foodtruck.backend.presentation.exception.product.ProductExceptions.*;

@Service
@Transactional
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private FileStorageService fileStorageService;

    /**
     * Obtiene todos los productos activos del sistema
     * 
     * @return Lista de productos activos con información completa
     */
    @Transactional(readOnly = true)
    public List<ProductResponse> findAllActiveProducts() {
        return productRepository.findActiveWithCategory()
                .stream()
                .map(this::mapToProductResponse)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene todos los productos del sistema (activos e inactivos)
     * 
     * @return Lista de todos los productos con información completa
     */
    @Transactional(readOnly = true)
    public List<ProductResponse> findAllProducts() {
        return productRepository.findAllWithCategory()
                .stream()
                .map(this::mapToProductResponse)
                .collect(Collectors.toList());
    }

    /**
     * Busca un producto por su ID
     * 
     * @param id Identificador único del producto
     * @return Información completa del producto
     * @throws ProductNotFoundException si el producto no existe
     */
    @Transactional(readOnly = true)
    public ProductResponse findProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));

        return mapToProductResponse(product);
    }

    /**
     * Busca un producto activo por su ID
     * 
     * @param id Identificador único del producto
     * @return Información completa del producto activo
     * @throws ProductNotFoundException si el producto no existe o está inactivo
     */
    @Transactional(readOnly = true)
    public ProductResponse findActiveProductById(Long id) {
        Product product = productRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new ProductNotFoundException(id));

        return mapToProductResponse(product);
    }

    /**
     * Busca productos por ID de categoría
     * 
     * @param categoryId Identificador de la categoría
     * @return Lista de productos de la categoría especificada
     */
    @Transactional(readOnly = true)
    public List<ProductResponse> findProductsByCategory(Long categoryId) {
        return productRepository.findByCategoryIdWithCategory(categoryId)
                .stream()
                .map(this::mapToProductResponse)
                .collect(Collectors.toList());
    }

    /**
     * Busca productos activos por ID de categoría
     * 
     * @param categoryId Identificador de la categoría
     * @return Lista de productos activos de la categoría especificada
     */
    @Transactional(readOnly = true)
    public List<ProductResponse> findActiveProductsByCategory(Long categoryId) {
        return productRepository.findByCategoryIdAndActiveTrueWithCategory(categoryId)
                .stream()
                .map(this::mapToProductResponse)
                .collect(Collectors.toList());
    }

    /**
     * Busca productos por nombre (búsqueda parcial)
     * 
     * @param name Parte del nombre a buscar
     * @return Lista de productos que contienen el texto en su nombre
     */
    @Transactional(readOnly = true)
    public List<ProductSearchResponse> searchProductsByName(String name) {
        return productRepository.findByNameContainingIgnoreCase(name)
                .stream()
                .map(this::mapToProductSearchResponse)
                .collect(Collectors.toList());
    }

    /**
     * Busca productos activos por nombre (búsqueda parcial)
     * 
     * @param name Parte del nombre a buscar
     * @return Lista de productos activos que contienen el texto en su nombre
     */
    @Transactional(readOnly = true)
    public List<ProductSearchResponse> searchActiveProductsByName(String name) {
        return productRepository.findByNameContainingIgnoreCaseAndActiveTrue(name)
                .stream()
                .map(this::mapToProductSearchResponse)
                .collect(Collectors.toList());
    }

    /**
     * Búsqueda avanzada de productos con múltiples filtros
     * 
     * @param name       Parte del nombre a buscar (opcional)
     * @param categoryId ID de la categoría a filtrar (opcional)
     * @param active     Estado de activación a filtrar (opcional)
     * @return Lista de productos que cumplen con los filtros aplicados
     */
    @Transactional(readOnly = true)
    public List<ProductSearchResponse> searchProductsWithFilters(String name, Long categoryId, Boolean active) {
        return productRepository.findWithFilters(name, categoryId, active)
                .stream()
                .map(this::mapToProductSearchResponse)
                .collect(Collectors.toList());
    }

    /**
     * Crea un nuevo producto en el sistema
     * 
     * @param request Datos del producto a crear
     * @return Información completa del producto creado
     * @throws ProductAlreadyExistsException       si ya existe un producto con el
     *                                             mismo nombre
     * @throws CategoryNotFoundForProductException si la categoría no existe
     * @throws InvalidProductPriceException        si el precio no es válido
     * @throws EmptyProductNameException           si el nombre está vacío
     * @throws ProductNameTooLongException         si el nombre es muy largo
     */
    @Transactional
    public ProductResponse createProduct(ProductCreateRequest request) {
        validateProductCreation(request);

        Category category = findCategoryForProduct(request.categoryId());

        Product product = Product.builder()
                .name(request.name().trim())
                .price(request.price())
                .active(request.active())
                .category(category)
                .build();

        if (request.image() != null && !request.image().isEmpty()) {
            String imageUrl = fileStorageService.saveProductImage(request.image(), product.getName());
            product.setImage(imageUrl);
        }

        Product savedProduct = productRepository.save(product);
        return mapToProductResponse(savedProduct);
    }

    /**
     * Actualiza los datos básicos de un producto existente
     * 
     * @param id      Identificador del producto a actualizar
     * @param request Nuevos datos del producto
     * @return Información actualizada del producto
     * @throws ProductNotFoundException            si el producto no existe
     * @throws ProductAlreadyExistsException       si el nuevo nombre ya existe en
     *                                             otro producto
     * @throws CategoryNotFoundForProductException si la nueva categoría no existe
     * @throws ProductInactiveCategoryException    si la nueva categoría está
     *                                             inactiva
     * @throws InvalidProductPriceException        si el nuevo precio no es válido
     * @throws ProductNameTooLongException         si el nuevo nombre es muy largo
     */
    public ProductResponse updateProduct(Long id, ProductUpdateRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));

        validateProductUpdate(id, request);

        if (request.name() != null && !request.name().trim().isEmpty()) {
            product.setName(request.name().trim());
        }

        if (request.price() != null) {
            validatePrice(request.price());
            product.setPrice(request.price());
        }

        if (request.active() != null) {
            product.setActive(request.active());
        }

        if (request.categoryId() != null) {
            Category category = findCategoryForProduct(request.categoryId());
            product.setCategory(category);
        }

        Product updatedProduct = productRepository.save(product);
        return mapToProductResponse(updatedProduct);
    }

    /**
     * Actualiza la imagen de un producto
     * 
     * @param id      Identificador del producto
     * @param request Nueva imagen del producto
     * @return Respuesta con información de la actualización
     * @throws ProductNotFoundException     si el producto no existe
     * @throws InvalidProductImageException si la imagen no es válida
     */
    public ProductImageUpdateResponse updateProductImage(Long id, ProductImageUpdateRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));

        // Eliminar imagen anterior si existe
        if (product.getImage() != null && !product.getImage().isEmpty()) {
            fileStorageService.deleteProductImage(product.getImage());
        }

        String imageUrl = fileStorageService.saveProductImage(request.image(), product.getName());
        product.setImage(imageUrl);

        productRepository.save(product);

        return new ProductImageUpdateResponse(
                "Imagen actualizada correctamente",
                imageUrl,
                product.getId(),
                product.getName());
    }

    /**
     * Cambia el estado de activación de un producto (activar/desactivar)
     * 
     * @param id     Identificador del producto
     * @param active Nuevo estado de activación
     * @return Información actualizada del producto
     * @throws ProductNotFoundException si el producto no existe
     */
    public ProductResponse toggleProductStatus(Long id, Boolean active) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));

        product.setActive(active);
        Product updatedProduct = productRepository.save(product);

        return mapToProductResponse(updatedProduct);
    }

    /**
     * Elimina un producto del sistema
     * 
     * @param id Identificador del producto a eliminar
     * @throws ProductNotFoundException si el producto no existe
     */
    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));

        // Eliminar imagen si existe
        if (product.getImage() != null && !product.getImage().isEmpty()) {
            fileStorageService.deleteProductImage(product.getImage());
        }

        productRepository.delete(product);
    }

    /**
     * Cuenta el número total de productos en una categoría
     * 
     * @param categoryId Identificador de la categoría
     * @return Número de productos en la categoría
     */
    @Transactional(readOnly = true)
    public long countProductsByCategory(Long categoryId) {
        return productRepository.countByCategoryId(categoryId);
    }

    /**
     * Cuenta el número de productos activos en una categoría
     * 
     * @param categoryId Identificador de la categoría
     * @return Número de productos activos en la categoría
     */
    @Transactional(readOnly = true)
    public long countActiveProductsByCategory(Long categoryId) {
        return productRepository.countByCategoryIdAndActiveTrue(categoryId);
    }

    /**
     * Obtiene un resumen simple de todos los productos activos (sin información de
     * categoría)
     * Útil para dropdowns, selects o listados básicos
     * 
     * @return Lista de productos activos en formato simple
     */
    @Transactional(readOnly = true)
    public List<ProductSimpleResponse> findAllActiveProductsSimple() {
        return productRepository.findByActiveTrue()
                .stream()
                .map(this::mapToProductSimpleResponse)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene productos de una categoría en formato simple
     * 
     * @param categoryId Identificador de la categoría
     * @return Lista de productos en formato simple
     */
    @Transactional(readOnly = true)
    public List<ProductSimpleResponse> findSimpleProductsByCategory(Long categoryId) {
        return productRepository.findByCategoryIdAndActiveTrue(categoryId)
                .stream()
                .map(this::mapToProductSimpleResponse)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene productos para mostrar en tarjetas o grillas (con imagen)
     * 
     * @return Lista de productos en formato resumen
     */
    @Transactional(readOnly = true)
    public List<ProductSummary> findProductsSummary() {
        return productRepository.findByActiveTrue()
                .stream()
                .map(this::mapToProductSummary)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene productos destacados o recomendados en formato resumen
     * 
     * @param limit Número máximo de productos a retornar
     * @return Lista limitada de productos en formato resumen
     */
    @Transactional(readOnly = true)
    public List<ProductSummary> findFeaturedProducts(int limit) {
        return productRepository.findByActiveTrue()
                .stream()
                .limit(limit)
                .map(this::mapToProductSummary)
                .collect(Collectors.toList());
    }

    // Métodos de validación privados

    /**
     * Valida los datos para la creación de un producto
     * 
     * @param request Datos del producto a validar
     */
    private void validateProductCreation(ProductCreateRequest request) {
        validateProductName(request.name());
        validatePrice(request.price());
        validateUniqueProductName(request.name());
    }

    /**
     * Valida los datos para la actualización de un producto
     * 
     * @param id      ID del producto que se está actualizando
     * @param request Datos del producto a validar
     */
    private void validateProductUpdate(Long id, ProductUpdateRequest request) {
        if (request.name() != null) {
            validateProductName(request.name());
            validateUniqueProductNameForUpdate(id, request.name());
        }

        if (request.price() != null) {
            validatePrice(request.price());
        }
    }

    /**
     * Valida el nombre del producto
     * 
     * @param name Nombre a validar
     */
    private void validateProductName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new EmptyProductNameException();
        }

        if (name.length() > 120) {
            throw new ProductNameTooLongException(120);
        }
    }

    /**
     * Valida el precio del producto
     * 
     * @param price Precio a validar
     */
    private void validatePrice(BigDecimal price) {
        if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidProductPriceException();
        }
    }

    /**
     * Valida que el nombre del producto sea único
     * 
     * @param name Nombre a validar
     */
    private void validateUniqueProductName(String name) {
        if (productRepository.existsByNameIgnoreCase(name)) {
            throw new ProductAlreadyExistsException(name);
        }
    }

    /**
     * Valida que el nombre del producto sea único para actualización
     * 
     * @param id   ID del producto que se está actualizando
     * @param name Nuevo nombre a validar
     */
    private void validateUniqueProductNameForUpdate(Long id, String name) {
        if (productRepository.existsByNameIgnoreCaseAndIdNot(name, id)) {
            throw new ProductAlreadyExistsException(name);
        }
    }

    /**
     * Busca una categoría para asignar a un producto
     * 
     * @param categoryId ID de la categoría
     * @return La categoría encontrada
     */
    private Category findCategoryForProduct(Long categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new CategoryNotFoundForProductException(categoryId));
    }

    // Métodos de mapeo privados

    /**
     * Mapea una entidad Product a ProductResponse
     * 
     * @param product Entidad producto
     * @return DTO de respuesta del producto
     */
    private ProductResponse mapToProductResponse(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getPrice(),
                product.getImage(),
                product.getActive(),
                new CategoryInfo(product.getCategory().getId(), product.getCategory().getName()));
    }

    /**
     * Mapea una entidad Product a ProductSearchResponse
     * 
     * @param product Entidad producto
     * @return DTO de búsqueda del producto
     */
    private ProductSearchResponse mapToProductSearchResponse(Product product) {
        return new ProductSearchResponse(
                product.getId(),
                product.getName(),
                product.getPrice(),
                product.getImage(),
                product.getActive(),
                product.getCategory().getName());
    }

    /**
     * Mapea una entidad Product a ProductSimpleResponse
     * 
     * @param product Entidad producto
     * @return DTO simple del producto
     */
    private ProductSimpleResponse mapToProductSimpleResponse(Product product) {
        return new ProductSimpleResponse(
                product.getId(),
                product.getName(),
                product.getPrice(),
                product.getActive());
    }

    /**
     * Mapea una entidad Product a ProductSummary
     * 
     * @param product Entidad producto
     * @return DTO resumen del producto
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