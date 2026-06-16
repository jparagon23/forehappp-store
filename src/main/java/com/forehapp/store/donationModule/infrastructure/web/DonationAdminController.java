package com.forehapp.store.donationModule.infrastructure.web;

import com.forehapp.store.donationModule.application.dto.CreateFoundationRequestDto;
import com.forehapp.store.donationModule.application.dto.DonationRecordResponse;
import com.forehapp.store.donationModule.application.dto.FoundationResponse;
import com.forehapp.store.donationModule.application.dto.UpdateFoundationRequestDto;
import com.forehapp.store.donationModule.domain.ports.in.IDonationAdminService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/donations")
public class DonationAdminController {

    private final IDonationAdminService donationAdminService;

    public DonationAdminController(IDonationAdminService donationAdminService) {
        this.donationAdminService = donationAdminService;
    }

    // ── Foundations ────────────────────────────────────────────────────────────

    @PostMapping("/foundations")
    @ResponseStatus(HttpStatus.CREATED)
    public FoundationResponse createFoundation(
            @AuthenticationPrincipal String userId,
            @Valid @RequestBody CreateFoundationRequestDto dto) {
        return donationAdminService.createFoundation(Long.parseLong(userId), dto);
    }

    @GetMapping("/foundations")
    public List<FoundationResponse> listFoundations(@AuthenticationPrincipal String userId) {
        return donationAdminService.listFoundations(Long.parseLong(userId));
    }

    @GetMapping("/foundations/{foundationId}")
    public FoundationResponse getFoundation(
            @AuthenticationPrincipal String userId,
            @PathVariable Long foundationId) {
        return donationAdminService.getFoundation(Long.parseLong(userId), foundationId);
    }

    @PatchMapping("/foundations/{foundationId}")
    public FoundationResponse updateFoundation(
            @AuthenticationPrincipal String userId,
            @PathVariable Long foundationId,
            @Valid @RequestBody UpdateFoundationRequestDto dto) {
        return donationAdminService.updateFoundation(Long.parseLong(userId), foundationId, dto);
    }

    // ── Donation Records ───────────────────────────────────────────────────────

    @GetMapping("/records")
    public Page<DonationRecordResponse> listAllRecords(
            @AuthenticationPrincipal String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return donationAdminService.listAllDonationRecords(Long.parseLong(userId), page, size);
    }

    @GetMapping("/records/foundations/{foundationId}")
    public Page<DonationRecordResponse> listRecordsByFoundation(
            @AuthenticationPrincipal String userId,
            @PathVariable Long foundationId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return donationAdminService.listDonationRecordsByFoundation(Long.parseLong(userId), foundationId, page, size);
    }

    @PatchMapping("/records/{recordId}/pay")
    public DonationRecordResponse payDonation(
            @AuthenticationPrincipal String userId,
            @PathVariable Long recordId) {
        return donationAdminService.payDonation(Long.parseLong(userId), recordId);
    }
}
