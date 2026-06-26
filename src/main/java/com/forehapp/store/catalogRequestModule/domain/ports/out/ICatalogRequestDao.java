package com.forehapp.store.catalogRequestModule.domain.ports.out;

import com.forehapp.store.catalogRequestModule.domain.model.CatalogRequest;
import com.forehapp.store.catalogRequestModule.domain.model.CatalogRequestStatus;
import com.forehapp.store.catalogRequestModule.domain.model.CatalogRequestType;

import java.util.List;
import java.util.Optional;

public interface ICatalogRequestDao {
    CatalogRequest save(CatalogRequest request);
    Optional<CatalogRequest> findById(Long id);
    List<CatalogRequest> findByStoreId(Long storeId, CatalogRequestStatus status);
    List<CatalogRequest> findAll(CatalogRequestStatus status, CatalogRequestType type);
    boolean existsPendingByTypeAndName(CatalogRequestType type, String suggestedName);
    List<CatalogRequest> findPendingByTypeAndName(CatalogRequestType type, String suggestedName);
}
