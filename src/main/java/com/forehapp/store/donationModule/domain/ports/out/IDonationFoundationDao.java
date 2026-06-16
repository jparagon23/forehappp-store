package com.forehapp.store.donationModule.domain.ports.out;

import com.forehapp.store.donationModule.domain.model.DonationFoundation;

import java.util.List;
import java.util.Optional;

public interface IDonationFoundationDao {
    Optional<DonationFoundation> findById(Long id);
    Optional<DonationFoundation> findByName(String name);
    List<DonationFoundation> findAll();
    DonationFoundation save(DonationFoundation foundation);
}
