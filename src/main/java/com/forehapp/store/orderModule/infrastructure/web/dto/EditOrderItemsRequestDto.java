package com.forehapp.store.orderModule.infrastructure.web.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;

/**
 * The products the seller group should have after the edit. Lines with itemId keep (and may change) an
 * existing item; lines without it are added; existing items left out are removed.
 * reason is shown to the buyer.
 */
public record EditOrderItemsRequestDto(
        @NotBlank(message = "Reason is required") @Size(max = 500) String reason,
        @NotEmpty(message = "The order must keep at least one product") @Size(max = 50) @Valid List<Line> items
) {
    public record Line(
            Long itemId,
            @NotNull(message = "Variant is required") Long variantId,
            @NotNull @Min(1) @Max(9999) Integer quantity,
            @NotNull @DecimalMin(value = "0.0", message = "Price cannot be negative") BigDecimal unitPrice
    ) {}
}
