package com.forehapp.store.orderModule.infrastructure.web.dto;

import java.math.BigDecimal;

/**
 * balanceDue: only for paid orders; positive = buyer owes it, negative = store owes it back, null = settled.
 * paymentUrl: new Mercado Pago link when the order was unpaid and its total changed.
 */
public record EditOrderItemsResponseDto(
        Long orderId,
        Long groupId,
        BigDecimal groupSubtotal,
        BigDecimal orderTotalBefore,
        BigDecimal orderTotal,
        BigDecimal balanceDue,
        String paymentUrl
) {}
