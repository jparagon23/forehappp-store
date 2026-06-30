package com.forehapp.store.productModule.infrastructure.persistence;

import com.forehapp.store.productModule.domain.model.VariantCostHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IVariantCostHistoryRepository extends JpaRepository<VariantCostHistory, Long> {
    List<VariantCostHistory> findByVariantIdOrderByChangedAtDesc(Long variantId);
}
