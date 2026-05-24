package com.forehapp.store.storeModule.infrastructure.persistence;

import com.forehapp.store.storeModule.domain.model.Store;
import com.forehapp.store.storeModule.domain.ports.out.IStoreDao;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class StoreRepositoryAdapter implements IStoreDao {

    private final IStoreJpaRepository jpa;

    public StoreRepositoryAdapter(IStoreJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Store save(Store store) {
        return jpa.save(store);
    }

    @Override
    public Optional<Store> findById(Long storeId) {
        return jpa.findById(storeId);
    }

    @Override
    public boolean existsBySlug(String slug) {
        return jpa.existsBySlug(slug);
    }
}
