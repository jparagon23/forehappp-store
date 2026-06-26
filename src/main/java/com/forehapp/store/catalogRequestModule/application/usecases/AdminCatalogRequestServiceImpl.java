package com.forehapp.store.catalogRequestModule.application.usecases;

import com.forehapp.store.catalogRequestModule.application.dto.CatalogRequestResponse;
import com.forehapp.store.catalogRequestModule.application.dto.RejectCatalogRequestDto;
import com.forehapp.store.catalogRequestModule.domain.model.CatalogRequest;
import com.forehapp.store.catalogRequestModule.domain.model.CatalogRequestStatus;
import com.forehapp.store.catalogRequestModule.domain.model.CatalogRequestType;
import com.forehapp.store.catalogRequestModule.domain.ports.in.IAdminCatalogRequestService;
import com.forehapp.store.catalogRequestModule.domain.ports.out.ICatalogRequestDao;
import com.forehapp.store.general.exceptions.ConflictException;
import com.forehapp.store.general.exceptions.ErrorCode;
import com.forehapp.store.general.exceptions.ForbiddenException;
import com.forehapp.store.general.exceptions.NotFoundException;
import com.forehapp.store.notificationModule.domain.model.NotificationType;
import com.forehapp.store.notificationModule.infrastructure.services.PushNotificationService;
import com.forehapp.store.productModule.domain.model.Brand;
import com.forehapp.store.productModule.domain.model.Category;
import com.forehapp.store.productModule.domain.ports.out.IBrandDao;
import com.forehapp.store.productModule.domain.ports.out.ICategoryDao;
import com.forehapp.store.userModule.domain.model.StoreProfile;
import com.forehapp.store.userModule.domain.model.StoreRole;
import com.forehapp.store.userModule.domain.ports.out.IStoreProfileDao;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AdminCatalogRequestServiceImpl implements IAdminCatalogRequestService {

    private final ICatalogRequestDao catalogRequestDao;
    private final IStoreProfileDao storeProfileDao;
    private final IBrandDao brandDao;
    private final ICategoryDao categoryDao;
    private final PushNotificationService pushNotificationService;

    public AdminCatalogRequestServiceImpl(ICatalogRequestDao catalogRequestDao,
                                          IStoreProfileDao storeProfileDao,
                                          IBrandDao brandDao,
                                          ICategoryDao categoryDao,
                                          PushNotificationService pushNotificationService) {
        this.catalogRequestDao = catalogRequestDao;
        this.storeProfileDao = storeProfileDao;
        this.brandDao = brandDao;
        this.categoryDao = categoryDao;
        this.pushNotificationService = pushNotificationService;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CatalogRequestResponse> findAll(Long adminUserId, CatalogRequestStatus status, CatalogRequestType type) {
        requireAdmin(adminUserId);
        return catalogRequestDao.findAll(status, type).stream()
                .map(CatalogRequestServiceImpl::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public CatalogRequestResponse approve(Long adminUserId, Long requestId) {
        StoreProfile admin = requireAdmin(adminUserId);

        CatalogRequest request = findPending(requestId);

        Long resultId = createCatalogEntry(request);

        request.setStatus(CatalogRequestStatus.APPROVED);
        request.setResultId(resultId);
        request.setResolvedBy(admin);
        request.setResolvedAt(LocalDateTime.now());
        CatalogRequest saved = catalogRequestDao.save(request);

        autoRejectDuplicates(request, admin);

        notifySeller(request, true, null);

        return CatalogRequestServiceImpl.toResponse(saved);
    }

    @Override
    @Transactional
    public CatalogRequestResponse reject(Long adminUserId, Long requestId, RejectCatalogRequestDto dto) {
        StoreProfile admin = requireAdmin(adminUserId);

        CatalogRequest request = findPending(requestId);

        request.setStatus(CatalogRequestStatus.REJECTED);
        request.setRejectionReason(dto.reason());
        request.setResolvedBy(admin);
        request.setResolvedAt(LocalDateTime.now());
        CatalogRequest saved = catalogRequestDao.save(request);

        notifySeller(request, false, dto.reason());

        return CatalogRequestServiceImpl.toResponse(saved);
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private StoreProfile requireAdmin(Long userId) {
        StoreProfile profile = storeProfileDao.findByUserId(userId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.USER_PROFILE_NOT_FOUND,
                        "Store profile not found"));
        if (!profile.getRoles().contains(StoreRole.STORE_ADMIN)) {
            throw new ForbiddenException(ErrorCode.PRODUCT_CATALOG_ADMIN_REQUIRED,
                    "Access denied: STORE_ADMIN role required");
        }
        return profile;
    }

    private CatalogRequest findPending(Long requestId) {
        CatalogRequest request = catalogRequestDao.findById(requestId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.CATALOG_REQUEST_NOT_FOUND,
                        "Catalog request not found"));
        if (request.getStatus() != CatalogRequestStatus.PENDING) {
            throw new ConflictException(ErrorCode.CATALOG_REQUEST_NOT_PENDING,
                    "Only pending requests can be approved or rejected");
        }
        return request;
    }

    private Long createCatalogEntry(CatalogRequest request) {
        if (request.getType() == CatalogRequestType.BRAND) {
            if (brandDao.existsByDescriptionIgnoreCase(request.getSuggestedName())) {
                return brandDao.findAll().stream()
                        .filter(b -> b.getDescription().equalsIgnoreCase(request.getSuggestedName()))
                        .findFirst()
                        .map(Brand::getId)
                        .orElseThrow();
            }
            Brand brand = new Brand();
            brand.setDescription(request.getSuggestedName());
            return brandDao.save(brand).getId();
        } else {
            if (categoryDao.existsByDescriptionIgnoreCase(request.getSuggestedName())) {
                return categoryDao.findAll().stream()
                        .filter(c -> c.getDescription().equalsIgnoreCase(request.getSuggestedName()))
                        .findFirst()
                        .map(Category::getId)
                        .orElseThrow();
            }
            Category category = new Category();
            category.setDescription(request.getSuggestedName());
            return categoryDao.save(category).getId();
        }
    }

    private void autoRejectDuplicates(CatalogRequest approved, StoreProfile admin) {
        List<CatalogRequest> duplicates = catalogRequestDao.findPendingByTypeAndName(
                approved.getType(), approved.getSuggestedName());
        for (CatalogRequest dup : duplicates) {
            dup.setStatus(CatalogRequestStatus.REJECTED);
            dup.setRejectionReason("Approved via another request");
            dup.setResolvedBy(admin);
            dup.setResolvedAt(LocalDateTime.now());
            dup.setResultId(approved.getResultId());
            catalogRequestDao.save(dup);
            notifySeller(dup, false, "Approved via another request");
        }
    }

    private void notifySeller(CatalogRequest request, boolean approved, String reason) {
        Long sellerUserId = request.getRequestedBy().getUser().getId();
        String typeName = request.getType() == CatalogRequestType.BRAND ? "brand" : "category";
        String name = request.getSuggestedName();

        if (approved) {
            pushNotificationService.sendToUser(
                    sellerUserId,
                    "Request approved!",
                    "The " + typeName + " '" + name + "' is now available. You can add it to your product.",
                    "/seller/catalog-requests",
                    NotificationType.CATALOG_REQUEST_APPROVED
            );
        } else {
            String body = reason != null && !reason.isBlank()
                    ? "Your request for '" + name + "' was rejected: " + reason
                    : "Your request for '" + name + "' was rejected.";
            pushNotificationService.sendToUser(
                    sellerUserId,
                    "Request rejected",
                    body,
                    "/seller/catalog-requests",
                    NotificationType.CATALOG_REQUEST_REJECTED
            );
        }
    }
}
