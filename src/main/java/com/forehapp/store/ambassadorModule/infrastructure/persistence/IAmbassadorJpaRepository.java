package com.forehapp.store.ambassadorModule.infrastructure.persistence;

import com.forehapp.store.ambassadorModule.domain.model.Ambassador;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface IAmbassadorJpaRepository extends JpaRepository<Ambassador, Long> {
    Optional<Ambassador> findByStoreProfileId(Long profileId);
    Optional<Ambassador> findByReferralCode(String referralCode);
    boolean existsByReferralCode(String referralCode);
}
