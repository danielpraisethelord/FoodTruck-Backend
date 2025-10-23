package com.foodtruck.backend.application.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.foodtruck.backend.domain.model.Promotion;
import com.foodtruck.backend.domain.repository.PromotionRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class PromotionSchedulerService {

    private final PromotionRepository promotionRepository;

    /**
     * Ejecuta cada día a medianoche para desactivar promociones expiradas
     */
    @Scheduled(cron = "0 0 0 * * *") // Cada día a las 00:00
    @Transactional
    public void desactivateExpiredPromotions() {
        LocalDate today = LocalDate.now();

        List<Promotion> expiredPromotions = promotionRepository.findActiveTemporaryPromotionsEndingBefore(today);

        for (Promotion promotion : expiredPromotions) {
            promotion.setActive(false);
            log.info("Desactivada promoción expirada: ID {}", promotion.getId());
        }

        if (!expiredPromotions.isEmpty()) {
            promotionRepository.saveAll(expiredPromotions);
            log.info("Total de promociones expiradas desactivadas: {}", expiredPromotions.size());
        } else {
            log.info("No hay promociones expiradas para desactivar hoy.");
        }
    }
}
