package com.forehapp.store.orderModule.infrastructure.web.dto;

import com.forehapp.store.paymentModule.domain.model.PaymentMethod;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.util.List;

public record GuestCreateOrderRequestDto(
        @NotBlank(message = "Name is required") String name,
        @NotBlank(message = "Last name is required") String lastname,
        @NotBlank @Email(message = "Valid email is required") String email,
        @NotBlank(message = "Phone is required") String phone,
        @NotBlank(message = "Shipping address is required") String shippingAddress,
        @NotNull(message = "City is required") Long shippingCityId,
        String shippingComplement,
        String shippingReference,
        @NotEmpty(message = "Order must have at least one item")
        @Size(max = 50, message = "Too many items in a single order")
        @Valid List<GuestOrderItemDto> items,
        @NotNull(message = "Payment method is required") PaymentMethod paymentMethod,
        String couponCode,
        Long couponStoreId,
        String referralCode
) {}
