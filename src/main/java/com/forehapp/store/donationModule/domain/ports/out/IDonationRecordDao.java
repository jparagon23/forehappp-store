package com.forehapp.store.donationModule.domain.ports.out;

import com.forehapp.store.donationModule.domain.model.DonationRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface IDonationRecordDao {
    DonationRecord save(DonationRecord record);
    Optional<DonationRecord> findById(Long id);
    Page<DonationRecord> findAll(Pageable pageable);
    Page<DonationRecord> findByFoundationId(Long foundationId, Pageable pageable);
}
