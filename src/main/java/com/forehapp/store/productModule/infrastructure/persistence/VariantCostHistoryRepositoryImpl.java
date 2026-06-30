package com.forehapp.store.productModule.infrastructure.persistence;

import com.forehapp.store.productModule.domain.model.VariantCostHistory;
import com.forehapp.store.productModule.domain.ports.out.IVariantCostHistoryDao;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class VariantCostHistoryRepositoryImpl implements IVariantCostHistoryDao {

    private final IVariantCostHistoryRepository repository;

    public VariantCostHistoryRepositoryImpl(IVariantCostHistoryRepository repository) {
        this.repository = repository;
    }

    @Override
    public VariantCostHistory save(VariantCostHistory history) {
        return repository.save(history);
    }

    @Override
    public List<VariantCostHistory> findByVariantIdOrderByChangedAtDesc(Long variantId) {
        return repository.findByVariantIdOrderByChangedAtDesc(variantId);
    }
}
