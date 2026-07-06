package com.forehapp.store.ambassadorModule.application.usecases;

import com.forehapp.store.ambassadorModule.application.dto.*;
import com.forehapp.store.ambassadorModule.domain.model.Ambassador;
import com.forehapp.store.ambassadorModule.domain.model.AmbassadorCommission;
import com.forehapp.store.ambassadorModule.domain.model.AmbassadorStatus;
import com.forehapp.store.ambassadorModule.domain.model.CommissionStatus;
import com.forehapp.store.ambassadorModule.domain.ports.in.IAmbassadorAdminService;
import com.forehapp.store.ambassadorModule.domain.ports.out.IAmbassadorDao;
import com.forehapp.store.ambassadorModule.domain.ports.out.ICommissionDao;
import com.forehapp.store.general.exceptions.BadRequestException;
import com.forehapp.store.general.exceptions.ConflictException;
import com.forehapp.store.general.exceptions.ErrorCode;
import com.forehapp.store.general.exceptions.ForbiddenException;
import com.forehapp.store.general.exceptions.NotFoundException;
import com.forehapp.store.userModule.domain.model.StoreProfile;
import com.forehapp.store.userModule.domain.model.StoreRole;
import com.forehapp.store.userModule.domain.ports.out.IStoreProfileDao;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class AmbassadorAdminServiceImpl implements IAmbassadorAdminService {

    private final IAmbassadorDao ambassadorDao;
    private final ICommissionDao commissionDao;
    private final IStoreProfileDao storeProfileDao;

    public AmbassadorAdminServiceImpl(IAmbassadorDao ambassadorDao,
                                      ICommissionDao commissionDao,
                                      IStoreProfileDao storeProfileDao) {
        this.ambassadorDao = ambassadorDao;
        this.commissionDao = commissionDao;
        this.storeProfileDao = storeProfileDao;
    }

    @Override
    @Transactional
    public AmbassadorResponse create(Long adminUserId, CreateAmbassadorRequestDto dto) {
        requireAdmin(adminUserId);
        StoreProfile profile = storeProfileDao.findByUserId(dto.userId())
                .orElseThrow(() -> new NotFoundException(ErrorCode.USER_PROFILE_NOT_FOUND, "Store profile not found"));

        if (ambassadorDao.existsByReferralCode(dto.referralCode().toUpperCase())) {
            throw new ConflictException(ErrorCode.AMBASSADOR_REFERRAL_CODE_DUPLICATE,
                    "Referral code already in use: " + dto.referralCode());
        }

        ambassadorDao.findByProfileId(profile.getId()).ifPresent(existing -> {
            throw new ConflictException(ErrorCode.AMBASSADOR_ALREADY_EXISTS,
                    "This profile is already registered as an ambassador");
        });

        profile.getRoles().add(StoreRole.AMBASSADOR);
        storeProfileDao.save(profile);

        Ambassador ambassador = new Ambassador();
        ambassador.setStoreProfile(profile);
        ambassador.setReferralCode(dto.referralCode().toUpperCase());
        ambassador.setCommissionPercentage(dto.commissionPercentage());
        ambassador.setStatus(AmbassadorStatus.ACTIVE);

        Ambassador saved = ambassadorDao.save(ambassador);
        return toResponse(saved);
    }

    @Override
    @Transactional
    public AmbassadorResponse update(Long adminUserId, Long ambassadorId, UpdateAmbassadorRequestDto dto) {
        requireAdmin(adminUserId);
        Ambassador ambassador = findAmbassadorById(ambassadorId);

        if (dto.commissionPercentage() != null) {
            ambassador.setCommissionPercentage(dto.commissionPercentage());
        }

        if (dto.status() != null) {
            try {
                ambassador.setStatus(AmbassadorStatus.valueOf(dto.status()));
            } catch (IllegalArgumentException e) {
                throw new BadRequestException(ErrorCode.VALIDATION_ERROR,
                        "Invalid status value: " + dto.status());
            }
        }

        return toResponse(ambassadorDao.save(ambassador));
    }

    @Override
    @Transactional(readOnly = true)
    public List<AmbassadorResponse> findAll(Long adminUserId) {
        requireAdmin(adminUserId);
        return ambassadorDao.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public AmbassadorResponse findById(Long adminUserId, Long ambassadorId) {
        requireAdmin(adminUserId);
        return toResponse(findAmbassadorById(ambassadorId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommissionResponse> findCommissionsByAmbassador(Long adminUserId, Long ambassadorId) {
        requireAdmin(adminUserId);
        findAmbassadorById(ambassadorId);
        return commissionDao.findByAmbassadorId(ambassadorId).stream()
                .map(this::toCommissionResponse)
                .toList();
    }

    @Override
    @Transactional
    public CommissionResponse payCommission(Long adminUserId, Long commissionId) {
        requireAdmin(adminUserId);
        AmbassadorCommission commission = commissionDao.findById(commissionId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.COMMISSION_NOT_FOUND, "Commission not found"));

        if (commission.getStatus() == CommissionStatus.PAID) {
            throw new ConflictException(ErrorCode.COMMISSION_ALREADY_PAID, "Commission is already paid");
        }

        commission.setStatus(CommissionStatus.PAID);
        return toCommissionResponse(commissionDao.save(commission));
    }

    // ── helpers ────────────────────────────────────────────────────────────────

    private void requireAdmin(Long userId) {
        StoreProfile profile = storeProfileDao.findByUserId(userId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.USER_PROFILE_NOT_FOUND, "Store profile not found"));
        if (!profile.getRoles().contains(StoreRole.STORE_ADMIN)) {
            throw new ForbiddenException(ErrorCode.AMBASSADOR_ACCESS_DENIED, "STORE_ADMIN role required");
        }
    }

    private Ambassador findAmbassadorById(Long ambassadorId) {
        return ambassadorDao.findById(ambassadorId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.AMBASSADOR_NOT_FOUND, "Ambassador not found"));
    }

    private AmbassadorResponse toResponse(Ambassador ambassador) {
        List<AmbassadorCommission> commissions = ambassador.getCommissions();
        AmbassadorStatsDto stats = buildStats(ambassador, commissions);
        String name = ambassador.getStoreProfile().getUser().getName()
                + " " + ambassador.getStoreProfile().getUser().getLastname();
        return new AmbassadorResponse(
                ambassador.getId(),
                ambassador.getStoreProfile().getId(),
                name,
                ambassador.getStoreProfile().getUser().getEmail(),
                ambassador.getReferralCode(),
                ambassador.getCommissionPercentage(),
                ambassador.getStatus().name(),
                ambassador.getCreatedAt(),
                stats
        );
    }

    private AmbassadorStatsDto buildStats(Ambassador ambassador, List<AmbassadorCommission> commissions) {
        long totalOrders = commissions.size();
        BigDecimal totalEarned = commissions.stream()
                .map(AmbassadorCommission::getCommissionAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal pendingAmount = commissions.stream()
                .filter(c -> c.getStatus() == CommissionStatus.PENDING)
                .map(AmbassadorCommission::getCommissionAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal paidAmount = commissions.stream()
                .filter(c -> c.getStatus() == CommissionStatus.PAID)
                .map(AmbassadorCommission::getCommissionAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new AmbassadorStatsDto(
                ambassador.getReferralCode(),
                ambassador.getCommissionPercentage(),
                totalOrders,
                totalEarned,
                pendingAmount,
                paidAmount
        );
    }

    private CommissionResponse toCommissionResponse(AmbassadorCommission commission) {
        return new CommissionResponse(
                commission.getId(),
                commission.getOrderId(),
                commission.getCommissionAmount(),
                commission.getCommissionPercentage(),
                commission.getStatus().name(),
                commission.getCreatedAt()
        );
    }
}
