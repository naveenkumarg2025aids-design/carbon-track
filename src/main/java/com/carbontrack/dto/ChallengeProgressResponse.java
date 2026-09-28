package com.carbontrack.dto;

import java.math.BigDecimal;

public record ChallengeProgressResponse(Long challengeId, Long householdId,
        int comparisonYear, int comparisonMonth, BigDecimal previousMonthKgCo2e,
        BigDecimal currentMonthKgCo2e, BigDecimal reductionPercent,
        BigDecimal targetReductionPercent, boolean targetReached) {}
