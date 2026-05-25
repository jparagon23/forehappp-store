package com.forehapp.store.orderModule.infrastructure.persistence;

import com.forehapp.store.orderModule.domain.ports.out.IOrderItemDao;
import org.springframework.stereotype.Repository;

@Repository
public class OrderItemRepositoryImpl implements IOrderItemDao {

    private final IOrderItemRepository jpaRepository;

    public OrderItemRepositoryImpl(IOrderItemRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public boolean existsByVariantId(Long variantId) {
        return jpaRepository.existsByVariantId(variantId);
    }
}
