package com.forehapp.store.supplierSyncModule.infrastructure.persistence;

import com.forehapp.store.supplierSyncModule.domain.model.SupplierCode;
import com.forehapp.store.supplierSyncModule.domain.model.SupplierSyncRun;
import com.forehapp.store.supplierSyncModule.domain.model.SyncRunStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ISupplierSyncRunJpaRepository extends JpaRepository<SupplierSyncRun, Long> {

    List<SupplierSyncRun> findByStoreIdAndSupplierOrderByStartedAtDesc(Long storeId, SupplierCode supplier, Pageable pageable);

    Optional<SupplierSyncRun> findByIdAndStoreId(Long id, Long storeId);

    boolean existsByStoreIdAndSupplierAndStatus(Long storeId, SupplierCode supplier, SyncRunStatus status);

    // Catalog size of the last run whose data was accepted, used to spot a broken scrape
    @Query("SELECT r.supplierItems FROM SupplierSyncRun r WHERE r.supplier = :supplier " +
           "AND r.status <> com.forehapp.store.supplierSyncModule.domain.model.SyncRunStatus.ABORTED " +
           "ORDER BY r.startedAt DESC")
    List<Integer> findAcceptedCatalogSizes(@Param("supplier") SupplierCode supplier, Pageable pageable);
}
