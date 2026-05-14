package com.forehapp.store.reviewModule.application.dto;

public record ProductRatingSummary(
        Long productId,
        Double averageRating,
        Long totalReviews
) {}
