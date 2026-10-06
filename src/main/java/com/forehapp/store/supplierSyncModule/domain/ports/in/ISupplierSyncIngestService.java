package com.forehapp.store.supplierSyncModule.domain.ports.in;

import com.forehapp.store.supplierSyncModule.application.dto.IngestResponse;
import com.forehapp.store.supplierSyncModule.application.dto.SupplierCatalogIngestDto;
import com.forehapp.store.supplierSyncModule.application.dto.VariantForMatchingResponse;
import com.forehapp.store.supplierSyncModule.domain.model.SupplierCode;

import java.util.List;

/** Machine-to-machine operations used by the daily scraping script. */
public interface ISupplierSyncIngestService {
    IngestResponse ingest(SupplierCode supplier, SupplierCatalogIngestDto dto);
    List<VariantForMatchingResponse> variantsForMatching(SupplierCode supplier);
}
