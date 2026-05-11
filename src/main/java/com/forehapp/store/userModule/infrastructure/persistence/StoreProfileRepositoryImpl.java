package com.forehapp.store.userModule.infrastructure.persistence;

import com.forehapp.store.userModule.domain.model.StoreProfile;
import com.forehapp.store.userModule.domain.ports.out.IStoreProfileDao;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class StoreProfileRepositoryImpl implements IStoreProfileDao {

    private final IStoreProfileJpaRepository jpaRepository;

    public StoreProfileRepositoryImpl(IStoreProfileJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Optional<StoreProfile> findByUserId(Long userId) {
        return jpaRepository.findByUserId(userId);
    }
}
