package com.forehapp.store.inventoryModule.application.dto;

import com.forehapp.store.productModule.domain.model.MovementReason;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class AdjustInventoryRequestDto {

    @NotNull(message = "Quantity is required")
    private Integer quantity;

    @NotNull(message = "Reason is required")
    private MovementReason reason;
}
