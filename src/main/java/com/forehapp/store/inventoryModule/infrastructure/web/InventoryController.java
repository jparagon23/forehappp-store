package com.forehapp.store.inventoryModule.infrastructure.web;

import com.forehapp.store.inventoryModule.application.dto.AdjustInventoryRequestDto;
import com.forehapp.store.inventoryModule.domain.ports.in.IInventoryService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
public class InventoryController {

    private final IInventoryService inventoryService;

    public InventoryController(IInventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @PostMapping("/api/v1/inventory/products/{productId}/variants/{variantId}")
    public ResponseEntity<Void> sellerAdjustInventory(
            @PathVariable Long productId,
            @PathVariable Long variantId,
            @Valid @RequestBody AdjustInventoryRequestDto dto,
            @AuthenticationPrincipal String userId) {
        inventoryService.adjustInventory(productId, variantId, dto, Long.parseLong(userId));
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/api/v1/admin/inventory/products/{productId}/variants/{variantId}")
    public ResponseEntity<Void> adminAdjustInventory(
            @PathVariable Long productId,
            @PathVariable Long variantId,
            @Valid @RequestBody AdjustInventoryRequestDto dto,
            @AuthenticationPrincipal String userId) {
        inventoryService.adjustInventory(productId, variantId, dto, Long.parseLong(userId));
        return ResponseEntity.noContent().build();
    }
}
