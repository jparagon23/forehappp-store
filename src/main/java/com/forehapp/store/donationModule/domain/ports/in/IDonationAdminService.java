package com.forehapp.store.donationModule.domain.ports.in;

import com.forehapp.store.donationModule.application.dto.CreateFoundationRequestDto;
import com.forehapp.store.donationModule.application.dto.DonationRecordResponse;
import com.forehapp.store.donationModule.application.dto.FoundationResponse;
import com.forehapp.store.donationModule.application.dto.UpdateFoundationRequestDto;
import org.springframework.data.domain.Page;

import java.util.List;

public interface IDonationAdminService {
    FoundationResponse createFoundation(Long adminUserId, CreateFoundationRequestDto dto);
    FoundationResponse updateFoundation(Long adminUserId, Long foundationId, UpdateFoundationRequestDto dto);
    List<FoundationResponse> listFoundations(Long adminUserId);
    FoundationResponse getFoundation(Long adminUserId, Long foundationId);
    Page<DonationRecordResponse> listAllDonationRecords(Long adminUserId, int page, int size);
    Page<DonationRecordResponse> listDonationRecordsByFoundation(Long adminUserId, Long foundationId, int page, int size);
    DonationRecordResponse payDonation(Long adminUserId, Long donationRecordId);
}
