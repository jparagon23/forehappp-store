package com.forehapp.store.supplierSyncModule.domain.ports.in;

import com.forehapp.store.supplierSyncModule.application.dto.SupplierItemResponse;
import com.forehapp.store.supplierSyncModule.application.dto.SupplierLinkResponse;
import com.forehapp.store.supplierSyncModule.application.dto.SyncConfigDto;
import com.forehapp.store.supplierSyncModule.application.dto.SyncEventResponse;
import com.forehapp.store.supplierSyncModule.application.dto.SyncRunResponse;
import com.forehapp.store.supplierSyncModule.domain.model.SupplierCode;

import java.util.List;

/** Supplier pairing and sync status, managed by the store's OWNER or MANAGER. */
public interface ISellerSupplierSyncService {
    SyncConfigDto getConfig(Long storeId, SupplierCode supplier, Long userId);
    SyncConfigDto updateConfig(Long storeId, SupplierCode supplier, SyncConfigDto dto, Long userId);

    /** filter: SUGGESTED, CONFIRMED, REJECTED, NOT_SUPPLIED or UNLINKED (variants without any pair). */
    List<SupplierLinkResponse> listLinks(Long storeId, SupplierCode supplier, String filter, Long userId);
    List<SupplierLinkResponse> confirmLinks(Long storeId, SupplierCode supplier, List<Long> linkIds, Long userId);
    void rejectLink(Long storeId, SupplierCode supplier, Long linkId, Long userId);
    SupplierLinkResponse linkManually(Long storeId, SupplierCode supplier, Long variantId, Long supplierItemId, Long userId);
    void markNotSupplied(Long storeId, SupplierCode supplier, Long variantId, Long userId);
    void unlink(Long storeId, SupplierCode supplier, Long variantId, Long userId);

    List<SupplierItemResponse> searchItems(Long storeId, SupplierCode supplier, String query, Long userId);
    List<SyncRunResponse> listRuns(Long storeId, SupplierCode supplier, int limit, Long userId);
    List<SyncEventResponse> listRunEvents(Long storeId, Long runId, Long userId);
}
