package com.forehapp.store.catalogRequestModule.infrastructure.persistence;

import com.forehapp.store.catalogRequestModule.domain.model.CatalogRequest;
import com.forehapp.store.catalogRequestModule.domain.model.CatalogRequestStatus;
import com.forehapp.store.catalogRequestModule.domain.model.CatalogRequestType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ICatalogRequestJpaRepository extends JpaRepository<CatalogRequest, Long> {

    List<CatalogRequest> findByStoreIdOrderByCreatedAtDesc(Long storeId);

    List<CatalogRequest> findByStoreIdAndStatusOrderByCreatedAtDesc(Long storeId, CatalogRequestStatus status);

    List<CatalogRequest> findAllByOrderByCreatedAtDesc();

    List<CatalogRequest> findByStatusOrderByCreatedAtDesc(CatalogRequestStatus status);

    List<CatalogRequest> findByTypeOrderByCreatedAtDesc(CatalogRequestType type);

    List<CatalogRequest> findByStatusAndTypeOrderByCreatedAtDesc(CatalogRequestStatus status, CatalogRequestType type);

    boolean existsByTypeAndSuggestedNameIgnoreCaseAndStatus(
            CatalogRequestType type, String suggestedName, CatalogRequestStatus status);

    List<CatalogRequest> findByTypeAndSuggestedNameIgnoreCaseAndStatus(
            CatalogRequestType type, String suggestedName, CatalogRequestStatus status);
}
