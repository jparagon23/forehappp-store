package com.forehapp.store.inventoryModule.domain.ports.in;

import com.forehapp.store.inventoryModule.application.dto.AdjustInventoryRequestDto;

public interface IInventoryService {
    void adjustInventory(Long productId, Long variantId, AdjustInventoryRequestDto dto, Long userId);
}
