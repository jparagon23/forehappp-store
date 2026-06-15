package com.forehapp.store.orderModule.infrastructure.web.dto;

import com.forehapp.store.paymentModule.domain.model.PaymentMethod;
import jakarta.validation.constraints.NotNull;

public record CreateOrderRequestDto(
        @NotNull(message = "Address ID is required") Long addressId,
        @NotNull(message = "Payment method is required") PaymentMethod paymentMethod,
        String couponCode,
        Long couponStoreId,
        String referralCode
) {}
