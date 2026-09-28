package com.carbontrack.dto;

import java.math.BigDecimal;

public record LeaderboardEntryResponse(int rank, Long householdId, String householdName,
        BigDecimal previousMonthKgCo2e, BigDecimal currentMonthKgCo2e,
        BigDecimal reductionPercent) {}
