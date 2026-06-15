package com.forehapp.store.userModule.domain.ports.out;

import com.forehapp.store.userModule.domain.model.StoreProfile;

import java.util.Optional;

public interface IStoreProfileDao {
    Optional<StoreProfile> findById(Long profileId);
    Optional<StoreProfile> findByUserId(Long userId);
    StoreProfile save(StoreProfile storeProfile);
}
