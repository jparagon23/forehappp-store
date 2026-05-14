package com.forehapp.store.orderModule.domain.ports.in;

import com.forehapp.store.orderModule.infrastructure.web.dto.CreateOrderRequestDto;
import com.forehapp.store.orderModule.infrastructure.web.dto.OrderResponse;

public interface IOrderService {
    OrderResponse createOrder(Long userId, CreateOrderRequestDto dto);
    OrderResponse getOrderById(Long userId, Long orderId);
}
