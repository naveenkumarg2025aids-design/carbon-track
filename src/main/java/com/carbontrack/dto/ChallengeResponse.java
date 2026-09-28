package com.carbontrack.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ChallengeResponse(Long id, String title, String description, LocalDate startDate,
        LocalDate endDate, BigDecimal targetReductionPercent, boolean active) {}
