package com.carbontrack.dto;

import com.carbontrack.entity.CommuteMode;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

public record ActivityLogRequest(
        @NotNull Long householdId,
        @NotNull LocalDate activityDate,
        @NotNull CommuteMode commuteMode,
        @NotNull @DecimalMin("0.0") BigDecimal commuteDistanceKm,
        @NotNull @DecimalMin("0.0") BigDecimal electricityKwh,
        @NotNull @DecimalMin("0.0") BigDecimal wasteKg
) {}
