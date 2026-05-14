package com.forehapp.store.orderModule.domain.ports.in;

import com.forehapp.store.orderModule.infrastructure.web.dto.CreateOrderRequestDto;
import com.forehapp.store.orderModule.infrastructure.web.dto.OrderResponse;
import com.forehapp.store.orderModule.infrastructure.web.dto.OrderSummaryDto;

import java.util.List;

public interface IOrderService {
    OrderResponse createOrder(Long userId, CreateOrderRequestDto dto);
    OrderResponse getOrderById(Long userId, Long orderId);
    List<OrderSummaryDto> getMyOrders(Long userId);
}
