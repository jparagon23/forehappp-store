package com.forehapp.store.orderModule.infrastructure.web.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/**
 * Preview of a coupon for an assisted order, before creating it.
 * orderAmount: products subtotal of the order; shippingCost: its shipping (for free-shipping coupons).
 */
public record AssistedCouponValidateDto(
        @NotBlank @Email(message = "Valid email is required") String email,
        @NotBlank(message = "Coupon code is required") String code,
        @NotNull @DecimalMin("0.01") BigDecimal orderAmount,
        @DecimalMin("0.0") BigDecimal shippingCost
) {}
