package com.forehapp.store.productModule.application.dto;

import com.forehapp.store.productModule.domain.model.VariantCostHistory;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
public class VariantCostHistoryResponse {

    private final Long id;
    private final BigDecimal cost;
    private final String notes;
    private final LocalDateTime changedAt;

    public VariantCostHistoryResponse(VariantCostHistory history) {
        this.id = history.getId();
        this.cost = history.getCost();
        this.notes = history.getNotes();
        this.changedAt = history.getChangedAt();
    }
}
