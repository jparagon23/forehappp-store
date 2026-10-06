package com.forehapp.store.supplierSyncModule.infrastructure.persistence;

import com.forehapp.store.supplierSyncModule.domain.model.SupplierCode;
import com.forehapp.store.supplierSyncModule.domain.model.SupplierSyncConfig;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ISupplierSyncConfigJpaRepository extends JpaRepository<SupplierSyncConfig, Long> {

    Optional<SupplierSyncConfig> findByStoreIdAndSupplier(Long storeId, SupplierCode supplier);

    List<SupplierSyncConfig> findBySupplierAndEnabledTrue(SupplierCode supplier);
}
