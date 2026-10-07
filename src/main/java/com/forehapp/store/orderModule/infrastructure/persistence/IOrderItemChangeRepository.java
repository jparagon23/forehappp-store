package com.forehapp.store.orderModule.infrastructure.persistence;

import com.forehapp.store.orderModule.domain.model.OrderItemChange;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IOrderItemChangeRepository extends JpaRepository<OrderItemChange, Long> {
    List<OrderItemChange> findByGroupIdOrderByChangedAtDescIdAsc(Long groupId);
}
