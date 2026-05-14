package com.forehapp.store.orderModule.infrastructure.persistence;

import com.forehapp.store.orderModule.domain.model.OrderSellerGroup;
import com.forehapp.store.orderModule.domain.ports.out.IOrderGroupDao;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class OrderGroupRepositoryImpl implements IOrderGroupDao {

    private final IOrderSellerGroupRepository jpaRepository;

    public OrderGroupRepositoryImpl(IOrderSellerGroupRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Optional<OrderSellerGroup> findByIdWithDetails(Long groupId) {
        return jpaRepository.findByIdWithDetails(groupId);
    }

    @Override
    public List<OrderSellerGroup> findAllBySellerIdWithDetails(Long sellerId) {
        return jpaRepository.findAllBySellerIdWithDetails(sellerId);
    }

    @Override
    public List<OrderSellerGroup> findAllByOrderId(Long orderId) {
        return jpaRepository.findAllByOrder_Id(orderId);
    }

    @Override
    public OrderSellerGroup save(OrderSellerGroup group) {
        return jpaRepository.save(group);
    }
}
