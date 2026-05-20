package com.forehapp.store.storeModule.infrastructure.persistence;

import com.forehapp.store.storeModule.domain.model.StoreMembership;
import com.forehapp.store.storeModule.domain.model.StoreMemberRole;
import com.forehapp.store.storeModule.domain.ports.out.IStoreMembershipDao;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class StoreMembershipRepositoryAdapter implements IStoreMembershipDao {

    private final IStoreMembershipJpaRepository jpa;

    public StoreMembershipRepositoryAdapter(IStoreMembershipJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public StoreMembership save(StoreMembership membership) {
        return jpa.save(membership);
    }

    @Override
    public Optional<StoreMembership> findActiveByStoreIdAndStoreProfileId(Long storeId, Long storeProfileId) {
        return jpa.findByStoreIdAndStoreProfileIdAndActiveTrue(storeId, storeProfileId);
    }

    @Override
    public Optional<StoreMembership> findActiveByStoreIdAndUserId(Long storeId, Long userId) {
        return jpa.findActiveByStoreIdAndUserId(storeId, userId);
    }

    @Override
    public List<StoreMembership> findActiveByStoreProfileId(Long storeProfileId) {
        return jpa.findByStoreProfileIdAndActiveTrue(storeProfileId);
    }

    @Override
    public List<StoreMembership> findActiveByStoreId(Long storeId) {
        return jpa.findByStoreIdAndActiveTrue(storeId);
    }

    @Override
    public Optional<StoreMembership> findByIdAndStoreId(Long membershipId, Long storeId) {
        return jpa.findByIdAndStoreId(membershipId, storeId);
    }

    @Override
    public long countActiveOwnersByStoreId(Long storeId) {
        return jpa.countByStoreIdAndRoleAndActiveTrue(storeId, StoreMemberRole.OWNER);
    }
}
