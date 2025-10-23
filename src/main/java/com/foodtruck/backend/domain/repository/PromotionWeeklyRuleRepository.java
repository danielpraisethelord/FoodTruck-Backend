package com.foodtruck.backend.domain.repository;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.foodtruck.backend.domain.model.PromotionWeeklyRule;

/**
 * Repositorio para la gestión de reglas semanales de promociones recurrentes.
 * Proporciona operaciones CRUD y consultas especializadas para el manejo
 * de horarios y días de la semana de las promociones recurrentes.
 * 
 */
public interface PromotionWeeklyRuleRepository extends JpaRepository<PromotionWeeklyRule, Long> {

        /**
         * Busca todas las reglas semanales asociadas a una promoción específica.
         * 
         * @param promotionId ID de la promoción
         * @return lista de reglas semanales de la promoción
         */
        List<PromotionWeeklyRule> findByPromotionId(Long promotionId);

        /**
         * Busca todas las reglas configuradas para un día específico de la semana.
         * 
         * @param dayOfWeek día de la semana a buscar
         * @return lista de reglas configuradas para el día especificado
         */
        List<PromotionWeeklyRule> findByDayOfWeek(DayOfWeek dayOfWeek);

        /**
         * Busca reglas activas para un día específico de la semana.
         * Solo incluye reglas de promociones que están activas.
         * 
         * @param dayOfWeek día de la semana a buscar
         * @return lista de reglas activas para el día especificado
         */
        @Query("SELECT r FROM PromotionWeeklyRule r WHERE r.dayOfWeek = :dayOfWeek AND r.promotion.isActive = true")
        List<PromotionWeeklyRule> findActiveRulesByDayOfWeek(@Param("dayOfWeek") DayOfWeek dayOfWeek);

        /**
         * Busca reglas que están activas en un momento específico del día.
         * Verifica que la hora actual esté dentro del rango de la regla
         * y que la promoción esté activa.
         * 
         * @param dayOfWeek   día de la semana
         * @param currentTime hora actual a verificar
         * @return lista de reglas activas en el momento especificado
         */
        @Query("SELECT r FROM PromotionWeeklyRule r WHERE r.dayOfWeek = :dayOfWeek AND " +
                        "r.startTime <= :currentTime AND r.endTime >= :currentTime AND r.promotion.isActive = true")
        List<PromotionWeeklyRule> findActiveRulesAtTime(
                        @Param("dayOfWeek") DayOfWeek dayOfWeek,
                        @Param("currentTime") LocalTime currentTime);

        /**
         * Busca reglas que tienen conflictos de horario con una nueva regla propuesta.
         * Útil para validar que no haya solapamiento de horarios en la misma promoción
         * y día.
         * 
         * @param promotionId ID de la promoción
         * @param dayOfWeek   día de la semana
         * @param startTime   hora de inicio de la nueva regla
         * @param endTime     hora de fin de la nueva regla
         * @return lista de reglas que tienen conflicto de horario
         */
        @Query("SELECT r FROM PromotionWeeklyRule r WHERE r.promotion.id = :promotionId AND " +
                        "r.dayOfWeek = :dayOfWeek AND " +
                        "((r.startTime <= :endTime AND r.endTime >= :startTime))")
        List<PromotionWeeklyRule> findConflictingRules(
                        @Param("promotionId") Long promotionId,
                        @Param("dayOfWeek") DayOfWeek dayOfWeek,
                        @Param("startTime") LocalTime startTime,
                        @Param("endTime") LocalTime endTime);

        /**
         * Busca reglas con conflictos de horario excluyendo una regla específica.
         * Útil para validaciones durante actualizaciones de reglas existentes.
         * 
         * @param promotionId   ID de la promoción
         * @param excludeRuleId ID de la regla a excluir de la búsqueda
         * @param dayOfWeek     día de la semana
         * @param startTime     hora de inicio
         * @param endTime       hora de fin
         * @return lista de reglas conflictivas excluyendo la regla especificada
         */
        @Query("SELECT r FROM PromotionWeeklyRule r WHERE r.promotion.id = :promotionId AND " +
                        "r.id != :excludeRuleId AND r.dayOfWeek = :dayOfWeek AND " +
                        "((r.startTime <= :endTime AND r.endTime >= :startTime))")
        List<PromotionWeeklyRule> findConflictingRulesExcluding(
                        @Param("promotionId") Long promotionId,
                        @Param("excludeRuleId") Long excludeRuleId,
                        @Param("dayOfWeek") DayOfWeek dayOfWeek,
                        @Param("startTime") LocalTime startTime,
                        @Param("endTime") LocalTime endTime);

        /**
         * Busca reglas que están completamente dentro de un rango de tiempo específico.
         * 
         * @param startTime hora de inicio del rango
         * @param endTime   hora de fin del rango
         * @return lista de reglas que están dentro del rango especificado
         */
        @Query("SELECT r FROM PromotionWeeklyRule r WHERE r.startTime >= :startTime AND r.endTime <= :endTime")
        List<PromotionWeeklyRule> findRulesByTimeRange(
                        @Param("startTime") LocalTime startTime,
                        @Param("endTime") LocalTime endTime);

        /**
         * Cuenta el número de reglas semanales asociadas a una promoción.
         * 
         * @param promotionId ID de la promoción
         * @return número de reglas semanales de la promoción
         */
        long countByPromotionId(Long promotionId);

        /**
         * Verifica si una promoción tiene reglas configuradas para un día específico.
         * 
         * @param promotionId ID de la promoción
         * @param dayOfWeek   día de la semana a verificar
         * @return true si la promoción tiene reglas para el día, false en caso
         *         contrario
         */
        boolean existsByPromotionIdAndDayOfWeek(Long promotionId, DayOfWeek dayOfWeek);

        /**
         * Elimina todas las reglas semanales asociadas a una promoción.
         * Útil para operaciones de limpieza o eliminación en cascada manual.
         * 
         * @param promotionId ID de la promoción cuyas reglas se eliminarán
         */
        void deleteByPromotionId(Long promotionId);

        /**
         * Busca reglas de una promoción ordenadas por día de la semana y hora de
         * inicio.
         * Útil para mostrar las reglas de forma organizada.
         * 
         * @param promotionId ID de la promoción
         * @return lista de reglas ordenadas por día y hora de inicio
         */
        @Query("SELECT r FROM PromotionWeeklyRule r WHERE r.promotion.id = :promotionId " +
                        "ORDER BY r.dayOfWeek, r.startTime")
        List<PromotionWeeklyRule> findByPromotionIdOrderedByDayAndTime(@Param("promotionId") Long promotionId);

        /**
         * Busca reglas que terminan después de una hora específica en un día dado.
         * 
         * @param dayOfWeek día de la semana
         * @param time      hora de referencia
         * @return lista de reglas que terminan después de la hora especificada
         */
        List<PromotionWeeklyRule> findByDayOfWeekAndEndTimeGreaterThan(DayOfWeek dayOfWeek, LocalTime time);

        /**
         * Busca reglas que inician antes de una hora específica en un día dado.
         * 
         * @param dayOfWeek día de la semana
         * @param time      hora de referencia
         * @return lista de reglas que inician antes de la hora especificada
         */
        List<PromotionWeeklyRule> findByDayOfWeekAndStartTimeLessThan(DayOfWeek dayOfWeek, LocalTime time);

        /**
         * Obtiene todos los días de la semana que tiene configurados una promoción.
         * Útil para mostrar un resumen de los días activos de una promoción recurrente.
         * 
         * @param promotionId ID de la promoción
         * @return lista de días de la semana únicos configurados para la promoción
         */
        @Query("SELECT DISTINCT r.dayOfWeek FROM PromotionWeeklyRule r WHERE r.promotion.id = :promotionId")
        List<DayOfWeek> findDistinctDaysByPromotionId(@Param("promotionId") Long promotionId);

        /**
         * Busca reglas que están activas en este momento exacto.
         * Verifica día, hora y que la promoción esté activa.
         * 
         * @param dayOfWeek   día actual de la semana
         * @param currentTime hora actual
         * @return lista de reglas activas en este momento
         */
        @Query("SELECT r FROM PromotionWeeklyRule r WHERE r.dayOfWeek = :dayOfWeek AND " +
                        "r.startTime <= :currentTime AND r.endTime >= :currentTime AND " +
                        "r.promotion.isActive = true")
        List<PromotionWeeklyRule> findCurrentlyActiveRules(
                        @Param("dayOfWeek") DayOfWeek dayOfWeek,
                        @Param("currentTime") LocalTime currentTime);
}