package com.forehapp.store.inventoryModule.application.dto;

import com.forehapp.store.productModule.domain.model.MovementReason;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter @Setter
public class InventoryMovementResponse {

    private Long id;
    private Integer quantity;
    private MovementReason reason;
    private LocalDateTime createdAt;
}
