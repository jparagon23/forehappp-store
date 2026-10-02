package com.forehapp.store.repurchaseModule.domain.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/** Everything one buyer should be reminded about in a single email. */
public record ReminderPlan(String email, String name, List<Item> items) {

    public record Item(
            Long productId,
            String productTitle,
            BigDecimal currentPrice,
            int quantity,
            Long orderId,
            LocalDate purchasedOn,
            LocalDate dueDate,
            List<Long> orderItemIds
    ) {}
}
