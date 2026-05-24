package com.forehapp.store.storeModule.domain.ports.out;

import com.forehapp.store.storeModule.domain.model.Store;

import java.util.Optional;

public interface IStoreDao {
    Store save(Store store);
    Optional<Store> findById(Long storeId);
    boolean existsBySlug(String slug);
}
