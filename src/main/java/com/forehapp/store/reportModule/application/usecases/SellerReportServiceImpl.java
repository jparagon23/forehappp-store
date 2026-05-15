package com.forehapp.store.reportModule.application.usecases;

import com.forehapp.store.reportModule.application.dto.BusinessSummaryResponse;
import com.forehapp.store.reportModule.application.dto.TopProductResponse;
import com.forehapp.store.reportModule.domain.ports.in.ISellerReportService;
import com.forehapp.store.reportModule.domain.ports.out.IReportDao;
import com.forehapp.store.userModule.domain.model.StoreProfile;
import com.forehapp.store.userModule.domain.model.StoreRole;
import com.forehapp.store.userModule.domain.ports.out.IStoreProfileDao;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
public class SellerReportServiceImpl implements ISellerReportService {

    private final IReportDao reportDao;
    private final IStoreProfileDao storeProfileDao;

    public SellerReportServiceImpl(IReportDao reportDao, IStoreProfileDao storeProfileDao) {
        this.reportDao = reportDao;
        this.storeProfileDao = storeProfileDao;
    }

    @Override
    @Transactional(readOnly = true)
    public BusinessSummaryResponse getMySummary(Long userId, LocalDate from, LocalDate to) {
        StoreProfile profile = requireSeller(userId);
        Long sellerId = profile.getId();
        LocalDateTime fromDt = from.atStartOfDay();
        LocalDateTime toDt = to.atTime(LocalTime.MAX);

        return new BusinessSummaryResponse(
                reportDao.countSellerOrders(sellerId, fromDt, toDt),
                reportDao.sumSellerRevenue(sellerId, fromDt, toDt),
                reportDao.avgSellerTicket(sellerId, fromDt, toDt),
                0L,
                reportDao.countSellerReturns(sellerId, fromDt, toDt),
                reportDao.sumSellerRefunded(sellerId, fromDt, toDt)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<TopProductResponse> getMyTopProducts(Long userId, LocalDate from, LocalDate to, int limit) {
        StoreProfile profile = requireSeller(userId);
        int safeLimit = Math.min(Math.max(limit, 1), 50);
        return reportDao.getSellerTopProducts(
                profile.getId(),
                from.atStartOfDay(),
                to.atTime(LocalTime.MAX),
                safeLimit
        );
    }

    private StoreProfile requireSeller(Long userId) {
        StoreProfile profile = storeProfileDao.findByUserId(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Store profile not found"));
        if (!profile.getRoles().contains(StoreRole.SELLER)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Seller access required");
        }
        return profile;
    }
}
