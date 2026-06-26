package com.forehapp.store.catalogRequestModule.infrastructure.persistence;

import com.forehapp.store.catalogRequestModule.domain.model.CatalogRequest;
import com.forehapp.store.catalogRequestModule.domain.model.CatalogRequestStatus;
import com.forehapp.store.catalogRequestModule.domain.model.CatalogRequestType;
import com.forehapp.store.catalogRequestModule.domain.ports.out.ICatalogRequestDao;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class CatalogRequestRepositoryImpl implements ICatalogRequestDao {

    private final ICatalogRequestJpaRepository jpaRepository;

    public CatalogRequestRepositoryImpl(ICatalogRequestJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public CatalogRequest save(CatalogRequest request) {
        return jpaRepository.save(request);
    }

    @Override
    public Optional<CatalogRequest> findById(Long id) {
        return jpaRepository.findById(id);
    }

    @Override
    public List<CatalogRequest> findByStoreId(Long storeId, CatalogRequestStatus status) {
        if (status == null) {
            return jpaRepository.findByStoreIdOrderByCreatedAtDesc(storeId);
        }
        return jpaRepository.findByStoreIdAndStatusOrderByCreatedAtDesc(storeId, status);
    }

    @Override
    public List<CatalogRequest> findAll(CatalogRequestStatus status, CatalogRequestType type) {
        if (status == null && type == null) {
            return jpaRepository.findAllByOrderByCreatedAtDesc();
        }
        if (status != null && type == null) {
            return jpaRepository.findByStatusOrderByCreatedAtDesc(status);
        }
        if (status == null) {
            return jpaRepository.findByTypeOrderByCreatedAtDesc(type);
        }
        return jpaRepository.findByStatusAndTypeOrderByCreatedAtDesc(status, type);
    }

    @Override
    public boolean existsPendingByTypeAndName(CatalogRequestType type, String suggestedName) {
        return jpaRepository.existsByTypeAndSuggestedNameIgnoreCaseAndStatus(
                type, suggestedName, CatalogRequestStatus.PENDING);
    }

    @Override
    public List<CatalogRequest> findPendingByTypeAndName(CatalogRequestType type, String suggestedName) {
        return jpaRepository.findByTypeAndSuggestedNameIgnoreCaseAndStatus(
                type, suggestedName, CatalogRequestStatus.PENDING);
    }
}
