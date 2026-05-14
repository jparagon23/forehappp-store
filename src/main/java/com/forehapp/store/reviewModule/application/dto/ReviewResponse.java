package com.forehapp.store.reviewModule.application.dto;

import java.time.LocalDateTime;

public record ReviewResponse(
        Long reviewId,
        Long productId,
        String productTitle,
        Long reviewerId,
        String reviewerName,
        Integer rating,
        String title,
        String comment,
        String status,
        LocalDateTime createdAt
) {}
