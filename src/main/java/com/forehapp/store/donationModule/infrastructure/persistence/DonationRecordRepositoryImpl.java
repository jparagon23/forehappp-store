package com.forehapp.store.donationModule.infrastructure.persistence;

import com.forehapp.store.donationModule.domain.model.DonationRecord;
import com.forehapp.store.donationModule.domain.ports.out.IDonationRecordDao;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class DonationRecordRepositoryImpl implements IDonationRecordDao {

    private final IDonationRecordJpaRepository jpaRepository;

    public DonationRecordRepositoryImpl(IDonationRecordJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public DonationRecord save(DonationRecord record) {
        return jpaRepository.save(record);
    }

    @Override
    public Optional<DonationRecord> findById(Long id) {
        return jpaRepository.findById(id);
    }

    @Override
    public Page<DonationRecord> findAll(Pageable pageable) {
        return jpaRepository.findAll(pageable);
    }

    @Override
    public Page<DonationRecord> findByFoundationId(Long foundationId, Pageable pageable) {
        return jpaRepository.findByFoundationIdOrderByCreatedAtDesc(foundationId, pageable);
    }
}
