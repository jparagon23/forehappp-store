package com.forehapp.store.ambassadorModule.application.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record CreateAmbassadorRequestDto(
        @NotNull(message = "User ID is required") Long userId,
        @NotBlank(message = "Referral code is required")
        @Size(min = 3, max = 50, message = "Referral code must be between 3 and 50 characters")
        @Pattern(regexp = "^[A-Z0-9_]+$", message = "Referral code must contain only uppercase letters, digits and underscores")
        String referralCode,
        @NotNull(message = "Commission percentage is required")
        @DecimalMin(value = "0.01", message = "Commission percentage must be greater than 0")
        @DecimalMax(value = "100.00", message = "Commission percentage cannot exceed 100")
        BigDecimal commissionPercentage
) {}
