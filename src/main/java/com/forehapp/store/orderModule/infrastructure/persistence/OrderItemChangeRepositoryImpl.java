package com.forehapp.store.orderModule.infrastructure.persistence;

import com.forehapp.store.orderModule.domain.model.OrderItemChange;
import com.forehapp.store.orderModule.domain.ports.out.IOrderItemChangeDao;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class OrderItemChangeRepositoryImpl implements IOrderItemChangeDao {

    private final IOrderItemChangeRepository jpaRepository;

    public OrderItemChangeRepositoryImpl(IOrderItemChangeRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public List<OrderItemChange> saveAll(List<OrderItemChange> changes) {
        return jpaRepository.saveAll(changes);
    }

    @Override
    public List<OrderItemChange> findByGroupId(Long groupId) {
        return jpaRepository.findByGroupIdOrderByChangedAtDescIdAsc(groupId);
    }
}
