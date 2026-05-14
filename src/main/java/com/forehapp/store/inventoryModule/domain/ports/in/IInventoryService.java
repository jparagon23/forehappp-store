package com.forehapp.store.inventoryModule.domain.ports.in;

import com.forehapp.store.inventoryModule.application.dto.AdjustInventoryRequestDto;
import com.forehapp.store.inventoryModule.application.dto.InventoryMovementResponse;
import com.forehapp.store.productModule.domain.model.MovementReason;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface IInventoryService {
    void adjustInventory(Long productId, Long variantId, AdjustInventoryRequestDto dto, Long userId);
    Page<InventoryMovementResponse> getMovements(Long productId, Long variantId, MovementReason reason, Pageable pageable, Long userId);
}
