package com.carbontrack.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record HouseholdRequest(
        @NotBlank String name,
        @Email String email,
        String location
) {}
