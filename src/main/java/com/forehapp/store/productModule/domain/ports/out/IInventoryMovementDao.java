package com.forehapp.store.productModule.domain.ports.out;

import com.forehapp.store.productModule.domain.model.InventoryMovement;

public interface IInventoryMovementDao {
    InventoryMovement save(InventoryMovement movement);
    void incrementStock(Long variantId, int delta);
}
