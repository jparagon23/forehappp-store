package com.forehapp.store.productModule.domain.ports.in;

import com.forehapp.store.productModule.application.dto.AddInventoryRequestDto;

public interface IInventoryService {
    void addInventory(Long productId, Long variantId, AddInventoryRequestDto dto, Long userId);
}
