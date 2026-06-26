package com.forehapp.store.catalogRequestModule.application.dto;

import java.time.LocalDateTime;

public record CatalogRequestResponse(
        Long id,
        String type,
        String suggestedName,
        String status,
        String storeName,
        String requestedByName,
        String rejectionReason,
        Long resultId,
        LocalDateTime createdAt,
        LocalDateTime resolvedAt
) {}
