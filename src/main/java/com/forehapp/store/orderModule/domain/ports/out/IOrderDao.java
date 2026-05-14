package com.forehapp.store.orderModule.domain.ports.out;

import com.forehapp.store.orderModule.domain.model.Order;

import java.util.List;
import java.util.Optional;

public interface IOrderDao {
    Order save(Order order);
    Optional<Order> findById(Long orderId);
    Optional<Order> findBasicById(Long orderId);
    List<Order> findAllByBuyerIdOrderByCreatedAtDesc(Long buyerId);
}
