package com.foodtruck.backend.domain.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.foodtruck.backend.domain.model.Product;

public interface ProductRepository extends JpaRepository<Product, Long> {

    /**
     * Obtiene todos los productos que están activos
     * 
     * @return Lista de productos activos
     */
    List<Product> findByActiveTrue();

    /**
     * Busca productos por ID de categoría
     * 
     * @param categoryId ID de la categoría
     * @return Lista de productos de la categoría especificada
     */
    List<Product> findByCategoryId(Long categoryId);

    /**
     * Busca productos activos por ID de categoría
     * 
     * @param categoryId ID de la categoría
     * @return Lista de productos activos de la categoría especificada
     */
    List<Product> findByCategoryIdAndActiveTrue(Long categoryId);

    /**
     * Busca productos por nombre (búsqueda parcial, insensible a mayúsculas)
     * 
     * @param name Parte del nombre a buscar
     * @return Lista de productos que contienen el texto en su nombre
     */
    @Query("SELECT p FROM Product p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<Product> findByNameContainingIgnoreCase(@Param("name") String name);

    /**
     * Busca productos activos por nombre (búsqueda parcial, insensible a
     * mayúsculas)
     * 
     * @param name Parte del nombre a buscar
     * @return Lista de productos activos que contienen el texto en su nombre
     */
    @Query("SELECT p FROM Product p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%')) AND p.active = true")
    List<Product> findByNameContainingIgnoreCaseAndActiveTrue(@Param("name") String name);

    /**
     * Busca un producto por ID solo si está activo
     * 
     * @param id ID del producto
     * @return Optional con el producto si existe y está activo
     */
    Optional<Product> findByIdAndActiveTrue(Long id);

    /**
     * Verifica si existe un producto con el nombre especificado (insensible a
     * mayúsculas)
     * 
     * @param name Nombre del producto a verificar
     * @return true si existe un producto con ese nombre
     */
    boolean existsByNameIgnoreCase(String name);

    /**
     * Verifica si existe un producto con el nombre especificado, excluyendo un ID
     * específico
     * Útil para validaciones en actualizaciones
     * 
     * @param name Nombre del producto a verificar
     * @param id   ID del producto a excluir de la búsqueda
     * @return true si existe otro producto con ese nombre
     */
    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END FROM Product p WHERE LOWER(p.name) = LOWER(:name) AND p.id != :id")
    boolean existsByNameIgnoreCaseAndIdNot(@Param("name") String name, @Param("id") Long id);

    /**
     * Obtiene todos los productos con su información de categoría cargada (JOIN
     * FETCH)
     * 
     * @return Lista de productos con categoría incluida
     */
    @Query("SELECT p FROM Product p JOIN FETCH p.category")
    List<Product> findAllWithCategory();

    /**
     * Obtiene productos activos con su información de categoría cargada
     * 
     * @return Lista de productos activos con categoría incluida
     */
    @Query("SELECT p FROM Product p JOIN FETCH p.category WHERE p.active = true")
    List<Product> findActiveWithCategory();

    /**
     * Obtiene productos por categoría con información de categoría cargada
     * 
     * @param categoryId ID de la categoría
     * @return Lista de productos de la categoría con información completa
     */
    @Query("SELECT p FROM Product p JOIN FETCH p.category WHERE p.category.id = :categoryId")
    List<Product> findByCategoryIdWithCategory(@Param("categoryId") Long categoryId);

    /**
     * Obtiene productos activos por categoría con información de categoría cargada
     * 
     * @param categoryId ID de la categoría
     * @return Lista de productos activos de la categoría con información completa
     */
    @Query("SELECT p FROM Product p JOIN FETCH p.category WHERE p.category.id = :categoryId AND p.active = true")
    List<Product> findByCategoryIdAndActiveTrueWithCategory(@Param("categoryId") Long categoryId);

    /**
     * Búsqueda avanzada de productos con múltiples filtros opcionales
     * 
     * @param name       Parte del nombre a buscar (puede ser null)
     * @param categoryId ID de la categoría a filtrar (puede ser null)
     * @param active     Estado de activación a filtrar (puede ser null)
     * @return Lista de productos que cumplen con los filtros aplicados
     */
    @Query("SELECT p FROM Product p JOIN FETCH p.category c WHERE " +
            "(:name IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
            "(:categoryId IS NULL OR p.category.id = :categoryId) AND " +
            "(:active IS NULL OR p.active = :active)")
    List<Product> findWithFilters(@Param("name") String name,
            @Param("categoryId") Long categoryId,
            @Param("active") Boolean active);

    /**
     * Cuenta el número total de productos en una categoría
     * 
     * @param categoryId ID de la categoría
     * @return Número de productos en la categoría
     */
    long countByCategoryId(Long categoryId);

    /**
     * Cuenta el número de productos activos en una categoría
     * 
     * @param categoryId ID de la categoría
     * @return Número de productos activos en la categoría
     */
    long countByCategoryIdAndActiveTrue(Long categoryId);
}