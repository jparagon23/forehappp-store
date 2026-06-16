package com.forehapp.store.donationModule.application.usecases;

import com.forehapp.store.donationModule.application.dto.CreateFoundationRequestDto;
import com.forehapp.store.donationModule.application.dto.DonationRecordResponse;
import com.forehapp.store.donationModule.application.dto.FoundationResponse;
import com.forehapp.store.donationModule.application.dto.UpdateFoundationRequestDto;
import com.forehapp.store.donationModule.domain.model.DonationFoundation;
import com.forehapp.store.donationModule.domain.model.DonationFoundationStatus;
import com.forehapp.store.donationModule.domain.model.DonationRecord;
import com.forehapp.store.donationModule.domain.model.DonationRecordStatus;
import com.forehapp.store.donationModule.domain.ports.in.IDonationAdminService;
import com.forehapp.store.donationModule.domain.ports.out.IDonationFoundationDao;
import com.forehapp.store.donationModule.domain.ports.out.IDonationRecordDao;
import com.forehapp.store.general.exceptions.BadRequestException;
import com.forehapp.store.general.exceptions.ConflictException;
import com.forehapp.store.general.exceptions.ErrorCode;
import com.forehapp.store.general.exceptions.ForbiddenException;
import com.forehapp.store.general.exceptions.NotFoundException;
import com.forehapp.store.userModule.domain.model.StoreProfile;
import com.forehapp.store.userModule.domain.model.StoreRole;
import com.forehapp.store.userModule.domain.ports.out.IStoreProfileDao;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DonationAdminServiceImpl implements IDonationAdminService {

    private final IDonationFoundationDao foundationDao;
    private final IDonationRecordDao recordDao;
    private final IStoreProfileDao storeProfileDao;

    public DonationAdminServiceImpl(IDonationFoundationDao foundationDao,
                                    IDonationRecordDao recordDao,
                                    IStoreProfileDao storeProfileDao) {
        this.foundationDao = foundationDao;
        this.recordDao = recordDao;
        this.storeProfileDao = storeProfileDao;
    }

    @Override
    @Transactional
    public FoundationResponse createFoundation(Long adminUserId, CreateFoundationRequestDto dto) {
        requireAdmin(adminUserId);
        if (foundationDao.findByName(dto.name()).isPresent()) {
            throw new ConflictException(ErrorCode.DONATION_FOUNDATION_DUPLICATE, "A foundation with this name already exists");
        }
        DonationFoundation foundation = new DonationFoundation();
        foundation.setName(dto.name().trim());
        foundation.setDescription(dto.description());
        return toFoundationResponse(foundationDao.save(foundation));
    }

    @Override
    @Transactional
    public FoundationResponse updateFoundation(Long adminUserId, Long foundationId, UpdateFoundationRequestDto dto) {
        requireAdmin(adminUserId);
        DonationFoundation foundation = findFoundationById(foundationId);
        if (dto.description() != null) {
            foundation.setDescription(dto.description());
        }
        if (dto.status() != null) {
            try {
                foundation.setStatus(DonationFoundationStatus.valueOf(dto.status()));
            } catch (IllegalArgumentException e) {
                throw new BadRequestException(ErrorCode.VALIDATION_ERROR, "Invalid status value: " + dto.status());
            }
        }
        return toFoundationResponse(foundationDao.save(foundation));
    }

    @Override
    @Transactional(readOnly = true)
    public List<FoundationResponse> listFoundations(Long adminUserId) {
        requireAdmin(adminUserId);
        return foundationDao.findAll().stream().map(this::toFoundationResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public FoundationResponse getFoundation(Long adminUserId, Long foundationId) {
        requireAdmin(adminUserId);
        return toFoundationResponse(findFoundationById(foundationId));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<DonationRecordResponse> listAllDonationRecords(Long adminUserId, int page, int size) {
        requireAdmin(adminUserId);
        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return recordDao.findAll(pageable).map(this::toRecordResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<DonationRecordResponse> listDonationRecordsByFoundation(Long adminUserId, Long foundationId, int page, int size) {
        requireAdmin(adminUserId);
        findFoundationById(foundationId);
        PageRequest pageable = PageRequest.of(page, size);
        return recordDao.findByFoundationId(foundationId, pageable).map(this::toRecordResponse);
    }

    @Override
    @Transactional
    public DonationRecordResponse payDonation(Long adminUserId, Long donationRecordId) {
        requireAdmin(adminUserId);
        DonationRecord record = recordDao.findById(donationRecordId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.DONATION_RECORD_NOT_FOUND, "Donation record not found"));
        if (record.getStatus() == DonationRecordStatus.PAID) {
            throw new ConflictException(ErrorCode.DONATION_ALREADY_PAID, "Donation is already marked as paid");
        }
        record.setStatus(DonationRecordStatus.PAID);
        return toRecordResponse(recordDao.save(record));
    }

    // ── helpers ────────────────────────────────────────────────────────────────

    private void requireAdmin(Long userId) {
        StoreProfile profile = storeProfileDao.findByUserId(userId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.USER_PROFILE_NOT_FOUND, "Store profile not found"));
        if (!profile.getRoles().contains(StoreRole.STORE_ADMIN)) {
            throw new ForbiddenException(ErrorCode.STORE_ADMIN_REQUIRED, "Admin access required");
        }
    }

    private DonationFoundation findFoundationById(Long id) {
        return foundationDao.findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorCode.DONATION_FOUNDATION_NOT_FOUND, "Foundation not found"));
    }

    private FoundationResponse toFoundationResponse(DonationFoundation f) {
        return new FoundationResponse(f.getId(), f.getName(), f.getDescription(), f.getStatus().name(), f.getCreatedAt());
    }

    private DonationRecordResponse toRecordResponse(DonationRecord r) {
        return new DonationRecordResponse(
                r.getId(),
                r.getFoundation().getId(),
                r.getFoundation().getName(),
                r.getOrderId(),
                r.getCouponCode(),
                r.getDonorProfile() != null ? r.getDonorProfile().getId() : null,
                r.getDonorEmail(),
                r.getDonationAmount(),
                r.getDonationPercentage(),
                r.getStatus().name(),
                r.getCreatedAt()
        );
    }
}
