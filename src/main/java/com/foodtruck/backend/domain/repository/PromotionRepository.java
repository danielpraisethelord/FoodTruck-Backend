package com.foodtruck.backend.domain.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.foodtruck.backend.domain.model.Promotion;
import com.foodtruck.backend.domain.types.PromotionType;

/**
 * Repositorio para la gestión de promociones del sistema.
 * Proporciona operaciones CRUD y consultas especializadas para promociones
 * tanto temporales como recurrentes.
 * 
 */
public interface PromotionRepository extends JpaRepository<Promotion, Long> {

        /**
         * Busca promociones por su estado activo.
         * 
         * @param active estado activo de la promoción
         * @return lista de promociones que coinciden con el estado
         */
        List<Promotion> findByIsActive(boolean active);

        /**
         * Busca promociones por su estado activo con paginación.
         * 
         * @param active   estado activo de la promoción
         * @param pageable información de paginación
         * @return página de promociones que coinciden con el estado
         */
        Page<Promotion> findByIsActive(boolean active, Pageable pageable);

        /**
         * Busca promociones por tipo específico.
         * 
         * @param type tipo de promoción (TEMPORARY o RECURRING)
         * @return lista de promociones del tipo especificado
         */
        List<Promotion> findByType(PromotionType type);

        /**
         * Busca promociones por tipo y estado activo.
         * 
         * @param type   tipo de promoción
         * @param active estado activo de la promoción
         * @return lista de promociones que coinciden con ambos criterios
         */
        List<Promotion> findByTypeAndIsActive(PromotionType type, boolean active);

        /**
         * Busca promociones temporales activas que tienen fecha de inicio definida.
         * 
         * @param type   tipo de promoción
         * @param active estado activo de la promoción
         * @return lista de promociones temporales con fecha de inicio
         */
        List<Promotion> findByTypeAndIsActiveAndStartsAtIsNotNull(PromotionType type, boolean active);

        /**
         * Busca promociones que están vigentes en la fecha actual.
         * Incluye promociones recurrentes activas y promociones temporales
         * que están dentro de su período de validez.
         * 
         * @param currentDate fecha actual para validar vigencia
         * @return lista de promociones válidas en la fecha especificada
         */
        @Query("SELECT p FROM Promotion p WHERE p.isActive = true AND " +
                        "(p.type = 'RECURRING' OR " +
                        "(p.type = 'TEMPORARY' AND " +
                        "(p.startsAt IS NULL OR p.startsAt <= :currentDate) AND " +
                        "(p.endsAt IS NULL OR p.endsAt >= :currentDate)))")
        List<Promotion> findCurrentlyValidPromotions(@Param("currentDate") LocalDate currentDate);

        /**
         * Busca promociones temporales que han expirado.
         * Útil para procesos de limpieza o desactivación automática.
         * 
         * @param currentDate fecha actual para determinar expiración
         * @return lista de promociones temporales expiradas
         */
        @Query("SELECT p FROM Promotion p WHERE p.type = 'TEMPORARY' AND p.isActive = true AND " +
                        "p.endsAt IS NOT NULL AND p.endsAt < :currentDate")
        List<Promotion> findExpiredTemporaryPromotions(@Param("currentDate") LocalDate currentDate);

        /**
         * Busca promociones temporales que se solapan con un rango de fechas.
         * 
         * @param startDate fecha de inicio del rango
         * @param endDate   fecha de fin del rango
         * @return lista de promociones que se solapan con el rango especificado
         */
        @Query("SELECT p FROM Promotion p WHERE p.type = 'TEMPORARY' AND " +
                        "((p.startsAt IS NULL OR p.startsAt <= :endDate) AND " +
                        "(p.endsAt IS NULL OR p.endsAt >= :startDate))")
        List<Promotion> findTemporaryPromotionsByDateRange(
                        @Param("startDate") LocalDate startDate,
                        @Param("endDate") LocalDate endDate);

        /**
         * Busca promociones activas que incluyen un producto específico.
         * 
         * @param productId ID del producto a buscar
         * @return lista de promociones activas que contienen el producto
         */
        @Query("SELECT p FROM Promotion p JOIN p.products prod WHERE prod.id = :productId AND p.isActive = true")
        List<Promotion> findActivePromotionsByProductId(@Param("productId") Long productId);

        /**
         * Busca promociones activas que incluyen cualquiera de los productos
         * especificados.
         * 
         * @param productIds lista de IDs de productos a buscar
         * @return lista de promociones activas que contienen alguno de los productos
         */
        @Query("SELECT DISTINCT p FROM Promotion p JOIN p.products prod WHERE prod.id IN :productIds AND p.isActive = true")
        List<Promotion> findActivePromotionsByProductIds(@Param("productIds") List<Long> productIds);

        /**
         * Busca promociones por nombre que contenga el texto especificado (búsqueda
         * insensible a mayúsculas).
         * 
         * @param name   texto a buscar en el nombre
         * @param active estado activo de la promoción
         * @return lista de promociones cuyo nombre contiene el texto
         */
        List<Promotion> findByNameContainingIgnoreCaseAndIsActive(String name, boolean active);

        /**
         * Busca promociones por nombre con paginación (búsqueda insensible a
         * mayúsculas).
         * 
         * @param name     texto a buscar en el nombre
         * @param active   estado activo de la promoción
         * @param pageable información de paginación
         * @return página de promociones cuyo nombre contiene el texto
         */
        Page<Promotion> findByNameContainingIgnoreCaseAndIsActive(String name, boolean active, Pageable pageable);

        /**
         * Busca una promoción por ID con sus productos precargados.
         * Evita el problema N+1 al cargar la relación de productos.
         * 
         * @param id ID de la promoción
         * @return Optional con la promoción y sus productos, o vacío si no existe
         */
        @Query("SELECT p FROM Promotion p LEFT JOIN FETCH p.products WHERE p.id = :id")
        Optional<Promotion> findByIdWithProducts(@Param("id") Long id);

        /**
         * Busca una promoción por ID con sus reglas semanales precargadas.
         * Evita el problema N+1 al cargar la relación de reglas semanales.
         * 
         * @param id ID de la promoción
         * @return Optional con la promoción y sus reglas semanales, o vacío si no
         *         existe
         */
        @Query("SELECT p FROM Promotion p LEFT JOIN FETCH p.weeklyRules WHERE p.id = :id")
        Optional<Promotion> findByIdWithWeeklyRules(@Param("id") Long id);

        /**
         * Busca una promoción por ID con todas sus relaciones precargadas.
         * Carga tanto productos como reglas semanales en una sola consulta.
         * 
         * @param id ID de la promoción
         * @return Optional con la promoción completa, o vacío si no existe
         */
        @Query("SELECT p FROM Promotion p " +
                        "LEFT JOIN FETCH p.products " +
                        "LEFT JOIN FETCH p.weeklyRules " +
                        "WHERE p.id = :id")
        Optional<Promotion> findByIdWithAllRelations(@Param("id") Long id);

        /**
         * Obtiene todas las promociones ordenadas por ID descendente (más recientes
         * primero).
         * 
         * @return lista de promociones ordenadas por fecha de creación
         */
        @Query("SELECT p FROM Promotion p ORDER BY p.id DESC")
        List<Promotion> findAllOrderByIdDesc();

        /**
         * Obtiene todas las promociones ordenadas por ID descendente con paginación.
         * 
         * @param pageable información de paginación
         * @return página de promociones ordenadas por fecha de creación
         */
        Page<Promotion> findAllByOrderByIdDesc(Pageable pageable);

        /**
         * Busca promociones activas ordenadas por precio ascendente.
         * 
         * @param active estado activo de la promoción
         * @return lista de promociones ordenadas de menor a mayor precio
         */
        List<Promotion> findByIsActiveOrderByPriceAsc(boolean active);

        /**
         * Busca promociones activas ordenadas por precio descendente.
         * 
         * @param active estado activo de la promoción
         * @return lista de promociones ordenadas de mayor a menor precio
         */
        List<Promotion> findByIsActiveOrderByPriceDesc(boolean active);

        /**
         * Cuenta el número de promociones por tipo.
         * 
         * @param type tipo de promoción a contar
         * @return número total de promociones del tipo especificado
         */
        long countByType(PromotionType type);

        /**
         * Cuenta el número de promociones por tipo y estado activo.
         * 
         * @param type   tipo de promoción
         * @param active estado activo
         * @return número de promociones que coinciden con ambos criterios
         */
        long countByTypeAndIsActive(PromotionType type, boolean active);

        /**
         * Verifica si existe una promoción activa con el nombre especificado (búsqueda
         * insensible a mayúsculas).
         * Útil para validar nombres únicos.
         * 
         * @param name   nombre a verificar
         * @param active estado activo
         * @return true si existe una promoción con ese nombre, false en caso contrario
         */
        boolean existsByNameIgnoreCaseAndIsActive(String name, boolean active);

        /**
         * Busca promociones que inician en una fecha específica.
         * 
         * @param startsAt fecha de inicio a buscar
         * @return lista de promociones que inician en la fecha especificada
         */
        List<Promotion> findByStartsAt(LocalDate startsAt);

        /**
         * Busca promociones que terminan en una fecha específica.
         * 
         * @param endsAt fecha de fin a buscar
         * @return lista de promociones que terminan en la fecha especificada
         */
        List<Promotion> findByEndsAt(LocalDate endsAt);

        /**
         * Busca promociones temporales que están próximas a expirar en un rango de
         * fechas.
         * Útil para enviar notificaciones de expiración próxima.
         * 
         * @param startDate fecha de inicio del rango de expiración
         * @param endDate   fecha de fin del rango de expiración
         * @return lista de promociones que expiran en el rango especificado
         */
        @Query("SELECT p FROM Promotion p WHERE p.type = 'TEMPORARY' AND p.isActive = true AND " +
                        "p.endsAt IS NOT NULL AND p.endsAt BETWEEN :startDate AND :endDate")
        List<Promotion> findPromotionsExpiringBetween(
                        @Param("startDate") LocalDate startDate,
                        @Param("endDate") LocalDate endDate);

        /**
         * Busca promociones temporales activas que terminan antes de una fecha dada.
         * 
         * @param date
         * @return
         */
        @Query("SELECT p FROM Promotion p WHERE p.isActive = true AND p.type = 'TEMPORARY' AND p.endsAt < :date")
        List<Promotion> findActiveTemporaryPromotionsEndingBefore(@Param("date") LocalDate date);
}