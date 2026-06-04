package com.forehapp.store.orderModule.domain.ports.in;

import com.forehapp.store.cartModule.application.dto.ShippingEstimateResponse;
import com.forehapp.store.orderModule.infrastructure.web.dto.GuestCreateOrderRequestDto;
import com.forehapp.store.orderModule.infrastructure.web.dto.GuestShippingEstimateRequestDto;
import com.forehapp.store.orderModule.infrastructure.web.dto.OrderResponse;

public interface IGuestCheckoutService {
    OrderResponse placeOrder(GuestCreateOrderRequestDto dto);
    ShippingEstimateResponse estimateShipping(GuestShippingEstimateRequestDto dto);
}
