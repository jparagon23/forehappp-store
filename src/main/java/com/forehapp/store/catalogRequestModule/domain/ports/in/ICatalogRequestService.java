package com.forehapp.store.catalogRequestModule.domain.ports.in;

import com.forehapp.store.catalogRequestModule.application.dto.CatalogRequestResponse;
import com.forehapp.store.catalogRequestModule.application.dto.CreateCatalogRequestDto;
import com.forehapp.store.catalogRequestModule.domain.model.CatalogRequestStatus;

import java.util.List;

public interface ICatalogRequestService {
    CatalogRequestResponse create(Long storeId, Long userId, CreateCatalogRequestDto dto);
    List<CatalogRequestResponse> findByStore(Long storeId, Long userId, CatalogRequestStatus status);
    void cancel(Long storeId, Long requestId, Long userId);
}
