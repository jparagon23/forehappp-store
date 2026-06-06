package com.forehapp.store.promotionModule.application.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record GuestValidateCouponRequestDto(
        @NotBlank @Email String email,
        @NotBlank String code,
        @NotNull Long storeId,
        @NotNull @DecimalMin("0.01") BigDecimal orderAmount,
        @DecimalMin("0.0") BigDecimal shippingCost
) {}
