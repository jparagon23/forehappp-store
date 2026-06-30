package com.forehapp.store.productModule.domain.ports.out;

import com.forehapp.store.productModule.domain.model.VariantCostHistory;

import java.util.List;

public interface IVariantCostHistoryDao {
    VariantCostHistory save(VariantCostHistory history);
    List<VariantCostHistory> findByVariantIdOrderByChangedAtDesc(Long variantId);
}
