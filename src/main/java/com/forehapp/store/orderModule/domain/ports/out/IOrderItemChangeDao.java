package com.forehapp.store.orderModule.domain.ports.out;

import com.forehapp.store.orderModule.domain.model.OrderItemChange;

import java.util.List;

public interface IOrderItemChangeDao {
    List<OrderItemChange> saveAll(List<OrderItemChange> changes);
    List<OrderItemChange> findByGroupId(Long groupId);
}
