package com.saraprojects.product_api.model;

import com.saraprojects.product_api.enums.PromotionStatus;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class PromotionTest {

    @Test
    void calculateStatus_nowBeforeStartDate_returnsScheduled() {
        LocalDateTime start = LocalDateTime.now().plusDays(2);
        LocalDateTime end = LocalDateTime.now().plusDays(5);

        Promotion promotion = Promotion.builder()
                .startDate(start)
                .endDate(end)
                .build();

        PromotionStatus status = promotion.calculateStatus();

        assertThat(status).isEqualTo(PromotionStatus.SCHEDULED);
    }

    @Test
    void calculateStatus_nowBetweenStartAndEndDate_returnsActive() {
        LocalDateTime start = LocalDateTime.now().minusDays(1);
        LocalDateTime end = LocalDateTime.now().plusDays(2);

        Promotion promotion = Promotion.builder()
                .startDate(start)
                .endDate(end)
                .build();

        PromotionStatus status = promotion.calculateStatus();

        assertThat(status).isEqualTo(PromotionStatus.ACTIVE);
    }

    @Test
    void calculateStatus_nowAfterEndDate_returnsFinished() {
        LocalDateTime start = LocalDateTime.now().minusDays(5);
        LocalDateTime end = LocalDateTime.now().minusDays(1);

        Promotion promotion = Promotion.builder()
                .startDate(start)
                .endDate(end)
                .build();

        PromotionStatus status = promotion.calculateStatus();

        assertThat(status).isEqualTo(PromotionStatus.FINISHED);
    }
}
