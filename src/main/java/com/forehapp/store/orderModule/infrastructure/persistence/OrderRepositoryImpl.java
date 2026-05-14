package com.forehapp.store.orderModule.infrastructure.persistence;

import com.forehapp.store.orderModule.domain.model.Order;
import com.forehapp.store.orderModule.domain.ports.out.IOrderDao;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class OrderRepositoryImpl implements IOrderDao {

    private final IOrderRepository jpaRepository;

    public OrderRepositoryImpl(IOrderRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Order save(Order order) {
        return jpaRepository.save(order);
    }

    @Override
    public Optional<Order> findById(Long orderId) {
        return jpaRepository.findByIdWithGroups(orderId);
    }

    @Override
    public Optional<Order> findBasicById(Long orderId) {
        return jpaRepository.findById(orderId);
    }

    @Override
    public List<Order> findAllByBuyerIdOrderByCreatedAtDesc(Long buyerId) {
        return jpaRepository.findAllByBuyer_IdOrderByCreatedAtDesc(buyerId);
    }
}
