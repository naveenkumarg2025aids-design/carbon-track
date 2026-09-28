package com.carbontrack.dto;

import java.math.BigDecimal;

public record MonthlyFootprintResponse(Long householdId, int year, int month,
        BigDecimal totalKgCo2e, int activityCount) {}
