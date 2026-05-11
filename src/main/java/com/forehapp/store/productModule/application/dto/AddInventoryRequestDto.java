package com.forehapp.store.productModule.application.dto;

import com.forehapp.store.productModule.domain.model.MovementReason;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class AddInventoryRequestDto {

    @NotNull(message = "Quantity is required")
    private Integer quantity;

    @NotNull(message = "Reason is required")
    private MovementReason reason;
}
