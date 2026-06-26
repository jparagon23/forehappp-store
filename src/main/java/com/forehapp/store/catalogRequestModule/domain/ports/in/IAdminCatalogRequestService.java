package com.forehapp.store.catalogRequestModule.domain.ports.in;

import com.forehapp.store.catalogRequestModule.application.dto.CatalogRequestResponse;
import com.forehapp.store.catalogRequestModule.application.dto.RejectCatalogRequestDto;
import com.forehapp.store.catalogRequestModule.domain.model.CatalogRequestStatus;
import com.forehapp.store.catalogRequestModule.domain.model.CatalogRequestType;

import java.util.List;

public interface IAdminCatalogRequestService {
    List<CatalogRequestResponse> findAll(Long adminUserId, CatalogRequestStatus status, CatalogRequestType type);
    CatalogRequestResponse approve(Long adminUserId, Long requestId);
    CatalogRequestResponse reject(Long adminUserId, Long requestId, RejectCatalogRequestDto dto);
}
