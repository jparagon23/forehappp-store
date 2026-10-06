package com.forehapp.store.orderModule.application.dto;

import com.forehapp.store.orderModule.domain.model.OrderChannel;
import com.forehapp.store.orderModule.infrastructure.web.dto.GuestCreateOrderRequestDto;
import com.forehapp.store.orderModule.infrastructure.web.dto.GuestOrderItemDto;
import com.forehapp.store.paymentModule.domain.model.PaymentMethod;
import com.forehapp.store.userModule.domain.model.StoreProfile;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Everything needed to place an order without a cart: a guest checkout from the web, or an
 * ASSISTED order a seller registers for a customer.
 *
 * buyer:             account to attach the order to; null keeps it as a guest order (linked later by email)
 * alreadyPaid:       CASH/TRANSFER order that is born paid
 * registeredByStore: store name shown in the buyer email of an ASSISTED order
 */
public record PlaceOrderCommand(
        String name,
        String lastname,
        String email,
        String phone,
        String shippingAddress,
        Long shippingCityId,
        String shippingComplement,
        String shippingReference,
        List<GuestOrderItemDto> items,
        PaymentMethod paymentMethod,
        String couponCode,
        Long couponStoreId,
        String referralCode,
        StoreProfile buyer,
        OrderChannel channel,
        Long createdByUserId,
        LocalDateTime dataConsentAt,
        boolean alreadyPaid,
        String registeredByStore
) {
    public static PlaceOrderCommand fromGuest(GuestCreateOrderRequestDto dto) {
        return new PlaceOrderCommand(dto.name(), dto.lastname(), dto.email(), dto.phone(),
                dto.shippingAddress(), dto.shippingCityId(), dto.shippingComplement(), dto.shippingReference(),
                dto.items(), dto.paymentMethod(), dto.couponCode(), dto.couponStoreId(), dto.referralCode(),
                null, OrderChannel.ONLINE, null, null, false, null);
    }
}
