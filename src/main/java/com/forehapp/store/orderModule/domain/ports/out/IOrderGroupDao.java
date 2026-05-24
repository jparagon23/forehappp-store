package com.forehapp.store.orderModule.domain.ports.out;

import com.forehapp.store.orderModule.domain.model.OrderSellerGroup;

import java.util.List;
import java.util.Optional;

public interface IOrderGroupDao {
    Optional<OrderSellerGroup> findByIdWithDetails(Long groupId);
    List<OrderSellerGroup> findAllByStoreIdWithDetails(Long storeId);
    List<OrderSellerGroup> findAllByOrderId(Long orderId);
    OrderSellerGroup save(OrderSellerGroup group);
}
