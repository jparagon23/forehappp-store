package com.forehapp.store.storeModule.infrastructure.persistence;

import com.forehapp.store.storeModule.domain.model.StoreMembership;
import com.forehapp.store.storeModule.domain.model.StoreMemberRole;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface IStoreMembershipJpaRepository extends JpaRepository<StoreMembership, Long> {

    @Query("SELECT m FROM StoreMembership m WHERE m.store.id = :storeId AND m.storeProfile.user.id = :userId AND m.active = true")
    Optional<StoreMembership> findActiveByStoreIdAndUserId(@Param("storeId") Long storeId, @Param("userId") Long userId);
    Optional<StoreMembership> findByStoreIdAndStoreProfileIdAndActiveTrue(Long storeId, Long storeProfileId);
    List<StoreMembership> findByStoreProfileIdAndActiveTrue(Long storeProfileId);
    List<StoreMembership> findByStoreIdAndActiveTrue(Long storeId);
    Optional<StoreMembership> findByIdAndStoreId(Long id, Long storeId);
    long countByStoreIdAndRoleAndActiveTrue(Long storeId, StoreMemberRole role);
}
