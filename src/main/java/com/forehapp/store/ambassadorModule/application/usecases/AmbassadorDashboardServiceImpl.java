package com.forehapp.store.ambassadorModule.application.usecases;

import com.forehapp.store.ambassadorModule.application.dto.AmbassadorStatsDto;
import com.forehapp.store.ambassadorModule.application.dto.AmbassadorValidationResponse;
import com.forehapp.store.ambassadorModule.application.dto.CommissionResponse;
import com.forehapp.store.ambassadorModule.domain.model.Ambassador;
import com.forehapp.store.ambassadorModule.domain.model.AmbassadorCommission;
import com.forehapp.store.ambassadorModule.domain.model.CommissionStatus;
import com.forehapp.store.ambassadorModule.domain.ports.in.IAmbassadorDashboardService;
import com.forehapp.store.ambassadorModule.domain.ports.out.IAmbassadorDao;
import com.forehapp.store.ambassadorModule.domain.ports.out.ICommissionDao;
import com.forehapp.store.ambassadorModule.domain.model.AmbassadorStatus;
import com.forehapp.store.general.exceptions.ErrorCode;
import com.forehapp.store.general.exceptions.ForbiddenException;
import com.forehapp.store.general.exceptions.NotFoundException;
import com.forehapp.store.userModule.domain.model.StoreProfile;
import com.forehapp.store.userModule.domain.ports.out.IStoreProfileDao;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class AmbassadorDashboardServiceImpl implements IAmbassadorDashboardService {

    private final IAmbassadorDao ambassadorDao;
    private final ICommissionDao commissionDao;
    private final IStoreProfileDao storeProfileDao;

    public AmbassadorDashboardServiceImpl(IAmbassadorDao ambassadorDao,
                                          ICommissionDao commissionDao,
                                          IStoreProfileDao storeProfileDao) {
        this.ambassadorDao = ambassadorDao;
        this.commissionDao = commissionDao;
        this.storeProfileDao = storeProfileDao;
    }

    @Override
    @Transactional(readOnly = true)
    public AmbassadorStatsDto getMyStats(Long userId) {
        Ambassador ambassador = resolveAmbassador(userId);
        List<AmbassadorCommission> commissions = commissionDao.findByAmbassadorId(ambassador.getId());
        return buildStats(ambassador, commissions);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommissionResponse> getMyCommissions(Long userId) {
        Ambassador ambassador = resolveAmbassador(userId);
        return commissionDao.findByAmbassadorId(ambassador.getId()).stream()
                .map(this::toCommissionResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public AmbassadorValidationResponse validateCode(String referralCode) {
        Ambassador ambassador = ambassadorDao.findByReferralCode(referralCode.toUpperCase())
                .orElseThrow(() -> new NotFoundException(ErrorCode.AMBASSADOR_NOT_FOUND,
                        "Ambassador not found for code: " + referralCode));
        if (ambassador.getStatus() != AmbassadorStatus.ACTIVE) {
            throw new NotFoundException(ErrorCode.AMBASSADOR_INACTIVE,
                    "Ambassador is not active");
        }
        String name = ambassador.getStoreProfile().getUser().getName()
                + " " + ambassador.getStoreProfile().getUser().getLastname();
        return new AmbassadorValidationResponse(ambassador.getReferralCode(), name);
    }

    // ── helpers ────────────────────────────────────────────────────────────────

    private Ambassador resolveAmbassador(Long userId) {
        StoreProfile profile = storeProfileDao.findByUserId(userId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.USER_PROFILE_NOT_FOUND, "Store profile not found"));
        if (!profile.getRoles().contains(com.forehapp.store.userModule.domain.model.StoreRole.AMBASSADOR)) {
            throw new ForbiddenException(ErrorCode.AMBASSADOR_ACCESS_DENIED, "AMBASSADOR role required");
        }
        return ambassadorDao.findByProfileId(profile.getId())
                .orElseThrow(() -> new NotFoundException(ErrorCode.AMBASSADOR_NOT_FOUND, "Ambassador profile not found"));
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
