package com.forehapp.store.donationModule.infrastructure.persistence;

import com.forehapp.store.donationModule.domain.model.DonationFoundation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface IDonationFoundationJpaRepository extends JpaRepository<DonationFoundation, Long> {
    Optional<DonationFoundation> findByName(String name);
}
