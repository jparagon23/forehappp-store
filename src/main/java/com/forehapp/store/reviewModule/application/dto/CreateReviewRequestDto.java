package com.forehapp.store.reviewModule.application.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateReviewRequestDto(
        @NotNull @Min(1) @Max(5) Integer rating,
        @Size(max = 100) String title,
        @Size(max = 1000) String comment
) {}
