package com.forehapp.store.supplierSyncModule.infrastructure.persistence;

import com.forehapp.store.supplierSyncModule.domain.model.SupplierCatalogItem;
import com.forehapp.store.supplierSyncModule.domain.model.SupplierCode;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface ISupplierCatalogItemJpaRepository extends JpaRepository<SupplierCatalogItem, Long> {

    List<SupplierCatalogItem> findBySupplier(SupplierCode supplier);

    List<SupplierCatalogItem> findByIdIn(Collection<Long> ids);

    @Query("SELECT i FROM SupplierCatalogItem i WHERE i.supplier = :supplier " +
           "AND (:q = '' OR i.nameKey LIKE CONCAT('%', :q, '%')) ORDER BY i.name ASC")
    List<SupplierCatalogItem> search(@Param("supplier") SupplierCode supplier, @Param("q") String q, Pageable pageable);
}
