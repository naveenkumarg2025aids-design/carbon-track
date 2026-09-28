package com.carbontrack.dto;

import com.carbontrack.entity.CommuteMode;
import java.math.BigDecimal;
import java.time.LocalDate;

public record ActivityLogResponse(Long id, Long householdId, LocalDate activityDate,
        CommuteMode commuteMode, BigDecimal commuteDistanceKm, BigDecimal electricityKwh,
        BigDecimal wasteKg, BigDecimal estimatedKgCo2e) {}
