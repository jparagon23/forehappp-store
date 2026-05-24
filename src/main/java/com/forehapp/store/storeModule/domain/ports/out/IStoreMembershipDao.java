package com.forehapp.store.storeModule.domain.ports.out;

import com.forehapp.store.storeModule.domain.model.StoreMembership;
import com.forehapp.store.storeModule.domain.model.StoreMemberRole;

import java.util.List;
import java.util.Optional;

public interface IStoreMembershipDao {
    StoreMembership save(StoreMembership membership);
    Optional<StoreMembership> findActiveByStoreIdAndStoreProfileId(Long storeId, Long storeProfileId);
    Optional<StoreMembership> findActiveByStoreIdAndUserId(Long storeId, Long userId);
    List<StoreMembership> findActiveByStoreProfileId(Long storeProfileId);
    List<StoreMembership> findActiveByStoreId(Long storeId);
    Optional<StoreMembership> findByIdAndStoreId(Long membershipId, Long storeId);
    long countActiveOwnersByStoreId(Long storeId);
}
