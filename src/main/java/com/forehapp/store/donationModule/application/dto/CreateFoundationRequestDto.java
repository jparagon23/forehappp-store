package com.forehapp.store.donationModule.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateFoundationRequestDto(
        @NotBlank @Size(max = 150) String name,
        @Size(max = 255) String description
) {}
