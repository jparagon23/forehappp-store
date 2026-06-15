package com.forehapp.store.ambassadorModule.domain.ports.out;

import com.forehapp.store.ambassadorModule.domain.model.Ambassador;

import java.util.List;
import java.util.Optional;

public interface IAmbassadorDao {
    Optional<Ambassador> findById(Long id);
    Optional<Ambassador> findByProfileId(Long profileId);
    Optional<Ambassador> findByReferralCode(String referralCode);
    List<Ambassador> findAll();
    boolean existsByReferralCode(String referralCode);
    Ambassador save(Ambassador ambassador);
}
