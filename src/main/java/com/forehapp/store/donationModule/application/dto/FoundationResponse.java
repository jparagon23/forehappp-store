package com.forehapp.store.donationModule.application.dto;

import java.time.LocalDateTime;

public record FoundationResponse(
        Long id,
        String name,
        String description,
        String status,
        LocalDateTime createdAt
) {}
