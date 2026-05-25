package com.forehapp.store.orderModule.infrastructure.persistence;

import com.forehapp.store.orderModule.domain.model.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IOrderItemRepository extends JpaRepository<OrderItem, Long> {
    boolean existsByVariantId(Long variantId);
}
