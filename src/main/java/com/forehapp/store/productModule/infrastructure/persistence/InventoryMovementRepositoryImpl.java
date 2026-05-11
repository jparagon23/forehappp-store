package com.forehapp.store.productModule.infrastructure.persistence;

import com.forehapp.store.productModule.domain.model.InventoryMovement;
import com.forehapp.store.productModule.domain.ports.out.IInventoryMovementDao;
import org.springframework.stereotype.Repository;

@Repository
public class InventoryMovementRepositoryImpl implements IInventoryMovementDao {

    private final IInventoryMovementRepository jpaRepository;

    public InventoryMovementRepositoryImpl(IInventoryMovementRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public InventoryMovement save(InventoryMovement movement) {
        return jpaRepository.save(movement);
    }

    @Override
    public void incrementStock(Long variantId, int delta) {
        jpaRepository.incrementStock(variantId, delta);
    }
}
