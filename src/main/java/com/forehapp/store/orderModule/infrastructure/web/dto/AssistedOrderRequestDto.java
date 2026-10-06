package com.forehapp.store.orderModule.infrastructure.web.dto;

import com.forehapp.store.paymentModule.domain.model.PaymentMethod;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * Order a seller registers for a customer who ordered outside the app.
 * alreadyPaid: the customer already paid (CASH or TRANSFER only), so the order is born paid.
 * dataConsent: the seller confirms the customer authorized the use of their personal data.
 */
public record AssistedOrderRequestDto(
        @NotBlank @Email(message = "Valid email is required") String email,
        @NotBlank(message = "Name is required") String name,
        @NotBlank(message = "Last name is required") String lastname,
        @NotBlank(message = "Phone is required") String phone,
        @NotBlank(message = "Shipping address is required") String shippingAddress,
        @NotNull(message = "City is required") Long shippingCityId,
        String shippingComplement,
        String shippingReference,
        @NotEmpty(message = "Order must have at least one item")
        @Size(max = 50, message = "Too many items in a single order")
        @Valid List<GuestOrderItemDto> items,
        @NotNull(message = "Payment method is required") PaymentMethod paymentMethod,
        boolean alreadyPaid,
        Boolean dataConsent
) {}
