package com.forehapp.store.orderModule.domain.ports.in;

import com.forehapp.store.orderModule.infrastructure.web.dto.AssistedCustomerResponse;
import com.forehapp.store.orderModule.infrastructure.web.dto.AssistedOrderRequestDto;
import com.forehapp.store.orderModule.infrastructure.web.dto.OrderResponse;

/** Orders a store's OWNER or MANAGER registers for customers who ordered outside the app. */
public interface IAssistedOrderService {
    AssistedCustomerResponse lookupCustomer(Long storeId, String email, Long userId);
    OrderResponse placeOrder(Long storeId, AssistedOrderRequestDto dto, Long userId);
}
