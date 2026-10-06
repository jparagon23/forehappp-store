package com.forehapp.store.supplierSyncModule.infrastructure.persistence;

import com.forehapp.store.supplierSyncModule.domain.model.SupplierSyncEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ISupplierSyncEventJpaRepository extends JpaRepository<SupplierSyncEvent, Long> {

    List<SupplierSyncEvent> findByRunIdOrderByTypeAscIdAsc(Long runId);
}
