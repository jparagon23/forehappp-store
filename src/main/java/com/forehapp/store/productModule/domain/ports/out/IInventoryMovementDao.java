package com.forehapp.store.productModule.domain.ports.out;

import com.forehapp.store.productModule.domain.model.InventoryMovement;
import com.forehapp.store.productModule.domain.model.MovementReason;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface IInventoryMovementDao {
    InventoryMovement save(InventoryMovement movement);
    void incrementStock(Long variantId, int delta);
    Page<InventoryMovement> findByVariant(Long variantId, MovementReason reason, Pageable pageable);
}
