package com.gamehubstore.promotion_mscv.config;

import com.gamehubstore.promotion_mscv.models.Promotion;
import com.gamehubstore.promotion_mscv.repositories.PromotionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;

@Component
public class DataLoader implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataLoader.class);

    private final PromotionRepository promotionRepository;

    public DataLoader(PromotionRepository promotionRepository) {
        this.promotionRepository = promotionRepository;
    }

    @Override
    public void run(String... args) {
        crearSiNoExiste(
                "GAMER10",
                "PORCENTAJE",
                new BigDecimal("10.00"),
                BigDecimal.ZERO,
                new BigDecimal("100000.00"),
                1000
        );

        crearSiNoExiste(
                "GAMEHUB5000",
                "MONTO",
                new BigDecimal("5000.00"),
                new BigDecimal("20000.00"),
                null,
                1000
        );

        log.info("Promociones verificadas en promotion-mscv ({} registros totales).", promotionRepository.count());
    }

    private void crearSiNoExiste(
            String code,
            String type,
            BigDecimal value,
            BigDecimal minimumAmount,
            BigDecimal maxDiscount,
            Integer maxUses
    ) {
        if (promotionRepository.existsByCodeIgnoreCase(code)) {
            return;
        }
        Promotion promo = new Promotion();
        promo.setCode(code);
        promo.setType(type);
        promo.setValue(value);
        promo.setStartDate(LocalDate.now().minusDays(1));
        promo.setEndDate(LocalDate.now().plusYears(1));
        promo.setMinimumAmount(minimumAmount);
        promo.setMaxDiscount(maxDiscount);
        promo.setMaxUses(maxUses);
        promo.setUsedCount(0);
        promo.setActive(true);
        promotionRepository.save(promo);
        log.info("Cupón promocional precargado: {} ({} {})", code, type, value);
    }
}
