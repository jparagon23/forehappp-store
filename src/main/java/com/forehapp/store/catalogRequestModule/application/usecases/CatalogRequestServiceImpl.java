package com.forehapp.store.catalogRequestModule.application.usecases;

import com.forehapp.store.catalogRequestModule.application.dto.CatalogRequestResponse;
import com.forehapp.store.catalogRequestModule.application.dto.CreateCatalogRequestDto;
import com.forehapp.store.catalogRequestModule.domain.model.CatalogRequest;
import com.forehapp.store.catalogRequestModule.domain.model.CatalogRequestStatus;
import com.forehapp.store.catalogRequestModule.domain.model.CatalogRequestType;
import com.forehapp.store.catalogRequestModule.domain.ports.in.ICatalogRequestService;
import com.forehapp.store.catalogRequestModule.domain.ports.out.ICatalogRequestDao;
import com.forehapp.store.general.exceptions.ConflictException;
import com.forehapp.store.general.exceptions.ErrorCode;
import com.forehapp.store.general.exceptions.ForbiddenException;
import com.forehapp.store.general.exceptions.NotFoundException;
import com.forehapp.store.productModule.domain.ports.out.IBrandDao;
import com.forehapp.store.productModule.domain.ports.out.ICategoryDao;
import com.forehapp.store.storeModule.domain.model.Store;
import com.forehapp.store.storeModule.domain.ports.out.IStoreMembershipDao;
import com.forehapp.store.storeModule.domain.ports.out.IStoreDao;
import com.forehapp.store.userModule.domain.model.StoreProfile;
import com.forehapp.store.userModule.domain.ports.out.IStoreProfileDao;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CatalogRequestServiceImpl implements ICatalogRequestService {

    private final ICatalogRequestDao catalogRequestDao;
    private final IStoreMembershipDao membershipDao;
    private final IStoreDao storeDao;
    private final IStoreProfileDao storeProfileDao;
    private final IBrandDao brandDao;
    private final ICategoryDao categoryDao;

    public CatalogRequestServiceImpl(ICatalogRequestDao catalogRequestDao,
                                     IStoreMembershipDao membershipDao,
                                     IStoreDao storeDao,
                                     IStoreProfileDao storeProfileDao,
                                     IBrandDao brandDao,
                                     ICategoryDao categoryDao) {
        this.catalogRequestDao = catalogRequestDao;
        this.membershipDao = membershipDao;
        this.storeDao = storeDao;
        this.storeProfileDao = storeProfileDao;
        this.brandDao = brandDao;
        this.categoryDao = categoryDao;
    }

    @Override
    @Transactional
    public CatalogRequestResponse create(Long storeId, Long userId, CreateCatalogRequestDto dto) {
        membershipDao.findActiveByStoreIdAndUserId(storeId, userId)
                .orElseThrow(() -> new ForbiddenException(ErrorCode.CATALOG_REQUEST_ACCESS_DENIED,
                        "Not an active member of this store"));

        String normalizedName = dto.suggestedName().trim();

        if (dto.type() == CatalogRequestType.BRAND && brandDao.existsByDescriptionIgnoreCase(normalizedName)) {
            throw new ConflictException(ErrorCode.CATALOG_REQUEST_NAME_ALREADY_EXISTS,
                    "A brand with this name already exists");
        }
        if (dto.type() == CatalogRequestType.CATEGORY && categoryDao.existsByDescriptionIgnoreCase(normalizedName)) {
            throw new ConflictException(ErrorCode.CATALOG_REQUEST_NAME_ALREADY_EXISTS,
                    "A category with this name already exists");
        }

        if (catalogRequestDao.existsPendingByTypeAndName(dto.type(), normalizedName)) {
            throw new ConflictException(ErrorCode.CATALOG_REQUEST_ALREADY_PENDING,
                    "A pending request for this " + dto.type().name().toLowerCase() + " already exists");
        }

        Store store = storeDao.findById(storeId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.STORE_NOT_FOUND, "Store not found"));
        StoreProfile profile = storeProfileDao.findByUserId(userId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.USER_PROFILE_NOT_FOUND, "Profile not found"));

        CatalogRequest request = new CatalogRequest();
        request.setType(dto.type());
        request.setSuggestedName(normalizedName);
        request.setRequestedBy(profile);
        request.setStore(store);

        return toResponse(catalogRequestDao.save(request));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CatalogRequestResponse> findByStore(Long storeId, Long userId, CatalogRequestStatus status) {
        membershipDao.findActiveByStoreIdAndUserId(storeId, userId)
                .orElseThrow(() -> new ForbiddenException(ErrorCode.CATALOG_REQUEST_ACCESS_DENIED,
                        "Not an active member of this store"));
        return catalogRequestDao.findByStoreId(storeId, status).stream()
                .map(CatalogRequestServiceImpl::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public void cancel(Long storeId, Long requestId, Long userId) {
        membershipDao.findActiveByStoreIdAndUserId(storeId, userId)
                .orElseThrow(() -> new ForbiddenException(ErrorCode.CATALOG_REQUEST_ACCESS_DENIED,
                        "Not an active member of this store"));

        CatalogRequest request = catalogRequestDao.findById(requestId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.CATALOG_REQUEST_NOT_FOUND,
                        "Catalog request not found"));

        if (!request.getStore().getId().equals(storeId)) {
            throw new ForbiddenException(ErrorCode.CATALOG_REQUEST_ACCESS_DENIED,
                    "Request does not belong to this store");
        }

        if (request.getStatus() != CatalogRequestStatus.PENDING) {
            throw new ConflictException(ErrorCode.CATALOG_REQUEST_NOT_PENDING,
                    "Only pending requests can be cancelled");
        }

        request.setStatus(CatalogRequestStatus.REJECTED);
        request.setRejectionReason("Cancelled by seller");
        request.setResolvedAt(LocalDateTime.now());
        catalogRequestDao.save(request);
    }

    static CatalogRequestResponse toResponse(CatalogRequest r) {
        String requesterName = r.getRequestedBy().getUser().getName()
                + " " + r.getRequestedBy().getUser().getLastname();
        return new CatalogRequestResponse(
                r.getId(),
                r.getType().name(),
                r.getSuggestedName(),
                r.getStatus().name(),
                r.getStore().getName(),
                requesterName,
                r.getRejectionReason(),
                r.getResultId(),
                r.getCreatedAt(),
                r.getResolvedAt()
        );
    }
}
