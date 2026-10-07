package com.forehapp.store.orderModule.infrastructure.web.dto;

import com.forehapp.store.orderModule.domain.model.OrderItemChange;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record OrderItemChangeDto(
        Long id,
        String editId,
        String type,
        String oldLabel,
        Integer oldQuantity,
        BigDecimal oldUnitPrice,
        String newLabel,
        Integer newQuantity,
        BigDecimal newUnitPrice,
        String reason,
        BigDecimal orderTotalBefore,
        BigDecimal orderTotalAfter,
        LocalDateTime changedAt
) {
    public static OrderItemChangeDto from(OrderItemChange c) {
        return new OrderItemChangeDto(c.getId(), c.getEditId(), c.getType().name(),
                c.getOldLabel(), c.getOldQuantity(), c.getOldUnitPrice(),
                c.getNewLabel(), c.getNewQuantity(), c.getNewUnitPrice(),
                c.getReason(), c.getOrderTotalBefore(), c.getOrderTotalAfter(), c.getChangedAt());
    }
}
