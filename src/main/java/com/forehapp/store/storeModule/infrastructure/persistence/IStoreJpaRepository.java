package com.forehapp.store.storeModule.infrastructure.persistence;

import com.forehapp.store.storeModule.domain.model.Store;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IStoreJpaRepository extends JpaRepository<Store, Long> {
    boolean existsBySlug(String slug);
}
