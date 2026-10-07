package com.forehapp.store.orderModule.domain.events;

import java.math.BigDecimal;
import java.util.List;

/**
 * A seller changed the products of an order. Sent to the buyer once per edit.
 * balanceDue: paid orders only (positive = buyer owes, negative = store owes back); paymentUrl: new
 * Mercado Pago link for unpaid orders; paymentPending: the order is not paid yet.
 */
public record OrderItemsChangedEvent(
        Long orderId,
        String buyerEmail,
        String buyerName,
        String storeName,
        String reason,
        List<Line> lines,
        BigDecimal orderTotalBefore,
        BigDecimal orderTotalAfter,
        boolean paymentPending,
        BigDecimal balanceDue,
        String paymentUrl
) {
    public record Line(String type, String oldLabel, Integer oldQuantity, BigDecimal oldUnitPrice,
                       String newLabel, Integer newQuantity, BigDecimal newUnitPrice) {}
}
