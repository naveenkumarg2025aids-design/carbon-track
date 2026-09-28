package com.carbontrack.dto;

import java.time.LocalDateTime;

public record HouseholdResponse(Long id, String name, String email, String location, LocalDateTime createdAt) {}
