package com.forehapp.store.orderModule.domain.ports.in;

import com.forehapp.store.orderModule.infrastructure.web.dto.AssistedCouponValidateDto;
import com.forehapp.store.orderModule.infrastructure.web.dto.AssistedCustomerResponse;
import com.forehapp.store.orderModule.infrastructure.web.dto.AssistedOrderRequestDto;
import com.forehapp.store.orderModule.infrastructure.web.dto.OrderResponse;
import com.forehapp.store.promotionModule.application.dto.CouponValidationResponse;

/** Orders a store's OWNER or MANAGER registers for customers who ordered outside the app. */
public interface IAssistedOrderService {
    AssistedCustomerResponse lookupCustomer(Long storeId, String email, Long userId);
    OrderResponse placeOrder(Long storeId, AssistedOrderRequestDto dto, Long userId);
    /** Previews a coupon with the rules the order will use (the customer's account or their email as guest). */
    CouponValidationResponse validateCoupon(Long storeId, AssistedCouponValidateDto dto, Long userId);
}
