package com.forehapp.store.orderModule.infrastructure.web.dto;

import java.time.LocalDateTime;

/**
 * Whether an email already has an active account (the order will go straight to it).
 * displayName is shortened ("Juan P.") so a seller cannot read customers' data by guessing emails.
 * lastOrder: contact and shipping data of the latest order with this email that has items of the
 * seller's store (data the seller already sees in their orders), to prefill the form; null if none.
 */
public record AssistedCustomerResponse(boolean registered, String displayName, LastOrderData lastOrder) {

    public record LastOrderData(
            Long orderId,
            LocalDateTime createdAt,
            String name,
            String lastname,
            String phone,
            String shippingAddress,
            String shippingComplement,
            String shippingReference,
            Long countryId,
            Long stateId,
            Long cityId
    ) {}
}
