package com.forehapp.store.orderModule.domain.ports.out;

public interface IOrderItemDao {
    boolean existsByVariantId(Long variantId);
}
