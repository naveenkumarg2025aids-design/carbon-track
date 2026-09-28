package com.carbontrack.dto;

import com.carbontrack.entity.ParticipationStatus;
import java.time.LocalDateTime;

public record ParticipationResponse(Long id, Long householdId, Long challengeId,
        LocalDateTime joinedAt, ParticipationStatus status) {}
