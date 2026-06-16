package com.forehapp.store.donationModule.infrastructure.persistence;

import com.forehapp.store.donationModule.domain.model.DonationFoundation;
import com.forehapp.store.donationModule.domain.ports.out.IDonationFoundationDao;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class DonationFoundationRepositoryImpl implements IDonationFoundationDao {

    private final IDonationFoundationJpaRepository jpaRepository;

    public DonationFoundationRepositoryImpl(IDonationFoundationJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Optional<DonationFoundation> findById(Long id) {
        return jpaRepository.findById(id);
    }

    @Override
    public Optional<DonationFoundation> findByName(String name) {
        return jpaRepository.findByName(name);
    }

    @Override
    public List<DonationFoundation> findAll() {
        return jpaRepository.findAll();
    }

    @Override
    public DonationFoundation save(DonationFoundation foundation) {
        return jpaRepository.save(foundation);
    }
}
