package com.foodtruck.backend.domain.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.foodtruck.backend.domain.model.Category;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    /**
     * Busca una categoría por su nombre exacto
     * 
     * @param name nombre de la categoría
     * @return Optional con la categoría encontrada
     */
    Optional<Category> findByName(String name);

    /**
     * Verifica si existe una categoría con el nombre dado
     * 
     * @param name nombre de la categoría
     * @return true si existe, false si no
     */
    boolean existsByName(String name);

    /**
     * Verifica si existe una categoría con el nombre dado, excluyendo un ID
     * específico
     * Útil para validaciones en actualizaciones
     * 
     * @param name nombre de la categoría
     * @param id   ID de la categoría a excluir
     * @return true si existe otra categoría con ese nombre
     */
    boolean existsByNameAndIdNot(String name, Long id);

    /**
     * Busca categorías que contengan el texto en su nombre (búsqueda parcial)
     * 
     * @param name texto a buscar en el nombre
     * @return lista de categorías que coinciden
     */
    List<Category> findByNameContainingIgnoreCase(String name);

    /**
     * Obtiene todas las categorías ordenadas por nombre ascendente
     * 
     * @return lista de categorías ordenadas
     */
    List<Category> findAllByOrderByNameAsc();

    /**
     * Obtiene categorías que tienen al menos un producto asociado
     * 
     * @return lista de categorías con productos
     */
    @Query("SELECT DISTINCT c FROM Category c WHERE SIZE(c.products) > 0")
    List<Category> findCategoriesWithProducts();

    /**
     * Obtiene categorías que no tienen productos asociados
     * 
     * @return lista de categorías sin productos
     */
    @Query("SELECT c FROM Category c WHERE SIZE(c.products) = 0")
    List<Category> findCategoriesWithoutProducts();

    /**
     * Obtiene una categoría con todos sus productos cargados (fetch join)
     * Útil para CategoryDetailedResponse
     * 
     * @param id ID de la categoría
     * @return Optional con la categoría y sus productos
     */
    @Query("SELECT c FROM Category c LEFT JOIN FETCH c.products WHERE c.id = :id")
    Optional<Category> findByIdWithProducts(@Param("id") Long id);

    /**
     * Obtiene todas las categorías con sus productos cargados
     * 
     * @return lista de categorías con productos
     */
    @Query("SELECT DISTINCT c FROM Category c LEFT JOIN FETCH c.products")
    List<Category> findAllWithProducts();

    /**
     * Cuenta el número de productos por categoría
     * 
     * @param categoryId ID de la categoría
     * @return número de productos en la categoría
     */
    @Query("SELECT COUNT(p) FROM Product p WHERE p.category.id = :categoryId")
    Long countProductsByCategoryId(@Param("categoryId") Long categoryId);

    /**
     * Obtiene categorías que tienen productos disponibles
     * 
     * @return lista de categorías con productos activos
     */
    @Query("SELECT DISTINCT c FROM Category c JOIN c.products p WHERE p.active = true")
    List<Category> findCategoriesWithActiveProducts();
}