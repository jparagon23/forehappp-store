package com.forehapp.store.reviewModule.application.dto;

import java.util.List;

public record ReviewPageResponse(
        ProductRatingSummary summary,
        List<ReviewResponse> reviews,
        int currentPage,
        int totalPages,
        long totalElements
) {}
