package com.forehapp.store.donationModule.infrastructure.persistence;

import com.forehapp.store.donationModule.domain.model.DonationRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IDonationRecordJpaRepository extends JpaRepository<DonationRecord, Long> {
    Page<DonationRecord> findByFoundationIdOrderByCreatedAtDesc(Long foundationId, Pageable pageable);
}
