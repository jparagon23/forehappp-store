package com.forehapp.store.ambassadorModule.infrastructure.persistence;

import com.forehapp.store.ambassadorModule.domain.model.Ambassador;
import com.forehapp.store.ambassadorModule.domain.ports.out.IAmbassadorDao;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class AmbassadorRepositoryImpl implements IAmbassadorDao {

    private final IAmbassadorJpaRepository jpaRepository;

    public AmbassadorRepositoryImpl(IAmbassadorJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Optional<Ambassador> findById(Long id) {
        return jpaRepository.findById(id);
    }

    @Override
    public Optional<Ambassador> findByProfileId(Long profileId) {
        return jpaRepository.findByStoreProfileId(profileId);
    }

    @Override
    public Optional<Ambassador> findByReferralCode(String referralCode) {
        return jpaRepository.findByReferralCode(referralCode);
    }

    @Override
    public List<Ambassador> findAll() {
        return jpaRepository.findAll();
    }

    @Override
    public boolean existsByReferralCode(String referralCode) {
        return jpaRepository.existsByReferralCode(referralCode);
    }

    @Override
    public Ambassador save(Ambassador ambassador) {
        return jpaRepository.save(ambassador);
    }
}
