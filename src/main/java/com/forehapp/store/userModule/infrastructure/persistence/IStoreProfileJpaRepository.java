package com.forehapp.store.userModule.infrastructure.persistence;

import com.forehapp.store.userModule.domain.model.StoreProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface IStoreProfileJpaRepository extends JpaRepository<StoreProfile, Long> {
    Optional<StoreProfile> findByUserId(Long userId);
}
