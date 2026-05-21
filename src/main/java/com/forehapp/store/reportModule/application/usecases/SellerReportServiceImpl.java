package com.forehapp.store.reportModule.application.usecases;

import com.forehapp.store.reportModule.application.dto.BusinessSummaryResponse;
import com.forehapp.store.reportModule.application.dto.TopProductResponse;
import com.forehapp.store.reportModule.domain.ports.in.ISellerReportService;
import com.forehapp.store.reportModule.domain.ports.out.IReportDao;
import com.forehapp.store.storeModule.domain.ports.out.IStoreMembershipDao;
import com.forehapp.store.general.exceptions.ErrorCode;
import com.forehapp.store.general.exceptions.ForbiddenException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
public class SellerReportServiceImpl implements ISellerReportService {

    private final IReportDao reportDao;
    private final IStoreMembershipDao membershipDao;

    public SellerReportServiceImpl(IReportDao reportDao, IStoreMembershipDao membershipDao) {
        this.reportDao = reportDao;
        this.membershipDao = membershipDao;
    }

    @Override
    @Transactional(readOnly = true)
    public BusinessSummaryResponse getMySummary(Long storeId, Long userId, LocalDate from, LocalDate to) {
        resolveStoreAccess(storeId, userId);
        LocalDateTime fromDt = from.atStartOfDay();
        LocalDateTime toDt = to.atTime(LocalTime.MAX);

        return new BusinessSummaryResponse(
                reportDao.countSellerOrders(storeId, fromDt, toDt),
                reportDao.sumSellerRevenue(storeId, fromDt, toDt),
                reportDao.avgSellerTicket(storeId, fromDt, toDt),
                0L,
                reportDao.countSellerReturns(storeId, fromDt, toDt),
                reportDao.sumSellerRefunded(storeId, fromDt, toDt)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<TopProductResponse> getMyTopProducts(Long storeId, Long userId, LocalDate from, LocalDate to, int limit) {
        resolveStoreAccess(storeId, userId);
        int safeLimit = Math.min(Math.max(limit, 1), 50);
        return reportDao.getSellerTopProducts(
                storeId,
                from.atStartOfDay(),
                to.atTime(LocalTime.MAX),
                safeLimit
        );
    }

    private void resolveStoreAccess(Long storeId, Long userId) {
        membershipDao.findActiveByStoreIdAndUserId(storeId, userId)
                .orElseThrow(() -> new ForbiddenException(ErrorCode.STORE_ACCESS_DENIED,
                        "You are not an active member of this store"));
    }
}
