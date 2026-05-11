package com.forehapp.store.userModule.domain.ports.out;

import com.forehapp.store.userModule.domain.model.StoreProfile;

import java.util.Optional;

public interface IStoreProfileDao {
    Optional<StoreProfile> findByUserId(Long userId);
}
