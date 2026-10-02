package com.forehapp.store.repurchaseModule.application.dto;

import java.time.LocalDate;
import java.util.List;

public record ReminderRunSummary(boolean dryRun, int emails, int products, List<Recipient> recipients) {

    public record Recipient(String email, List<Product> products) {}

    public record Product(Long productId, String title, Long orderId, LocalDate purchasedOn, LocalDate dueDate) {}
}
