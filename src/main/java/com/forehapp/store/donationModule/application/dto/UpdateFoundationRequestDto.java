package com.forehapp.store.donationModule.application.dto;

import jakarta.validation.constraints.Size;

public record UpdateFoundationRequestDto(
        @Size(max = 255) String description,
        String status
) {}
