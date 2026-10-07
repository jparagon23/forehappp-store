package com.forehapp.store.orderModule.domain.ports.in;

import com.forehapp.store.orderModule.infrastructure.web.dto.EditOrderItemsRequestDto;
import com.forehapp.store.orderModule.infrastructure.web.dto.EditOrderItemsResponseDto;
import com.forehapp.store.orderModule.infrastructure.web.dto.OrderItemChangeDto;

import java.util.List;

/** Seller changes to the products of an order (replace, quantity, price, remove, add). */
public interface IOrderItemsEditService {
    EditOrderItemsResponseDto editItems(Long storeId, Long groupId, EditOrderItemsRequestDto dto, Long userId);
    List<OrderItemChangeDto> getChanges(Long storeId, Long groupId, Long userId);
    /** The difference left by an edit to a paid order was collected or refunded by hand. */
    void settleBalance(Long storeId, Long groupId, Long userId);
}
