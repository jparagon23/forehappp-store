package com.forehapp.store.orderModule.domain.ports.in;

import com.forehapp.store.cartModule.application.dto.ShippingEstimateResponse;
import com.forehapp.store.orderModule.application.dto.PlaceOrderCommand;
import com.forehapp.store.orderModule.infrastructure.web.dto.GuestCreateOrderRequestDto;
import com.forehapp.store.orderModule.infrastructure.web.dto.GuestShippingEstimateRequestDto;
import com.forehapp.store.orderModule.infrastructure.web.dto.OrderResponse;

public interface IGuestCheckoutService {
    OrderResponse placeOrder(GuestCreateOrderRequestDto dto);
    /** Places an order without a cart (guest checkout or a seller-registered ASSISTED order). */
    OrderResponse place(PlaceOrderCommand command);
    ShippingEstimateResponse estimateShipping(GuestShippingEstimateRequestDto dto);
}
