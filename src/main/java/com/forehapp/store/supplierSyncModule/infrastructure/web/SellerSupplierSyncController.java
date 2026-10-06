package com.forehapp.store.supplierSyncModule.infrastructure.web;

import com.forehapp.store.supplierSyncModule.application.dto.ConfirmLinksDto;
import com.forehapp.store.supplierSyncModule.application.dto.ManualLinkDto;
import com.forehapp.store.supplierSyncModule.application.dto.SupplierItemResponse;
import com.forehapp.store.supplierSyncModule.application.dto.SupplierLinkResponse;
import com.forehapp.store.supplierSyncModule.application.dto.SyncConfigDto;
import com.forehapp.store.supplierSyncModule.application.dto.SyncEventResponse;
import com.forehapp.store.supplierSyncModule.application.dto.SyncRunResponse;
import com.forehapp.store.supplierSyncModule.domain.ports.in.ISellerSupplierSyncService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/stores/{storeId}/supplier-sync/{supplier}")
public class SellerSupplierSyncController {

    private final ISellerSupplierSyncService service;

    public SellerSupplierSyncController(ISellerSupplierSyncService service) {
        this.service = service;
    }

    @GetMapping("/config")
    public ResponseEntity<SyncConfigDto> getConfig(@PathVariable Long storeId, @PathVariable String supplier,
                                                   @AuthenticationPrincipal String userId) {
        return ResponseEntity.ok(service.getConfig(storeId, SupplierPaths.parse(supplier), Long.parseLong(userId)));
    }

    @PutMapping("/config")
    public ResponseEntity<SyncConfigDto> updateConfig(@PathVariable Long storeId, @PathVariable String supplier,
                                                      @Valid @RequestBody SyncConfigDto dto,
                                                      @AuthenticationPrincipal String userId) {
        return ResponseEntity.ok(service.updateConfig(storeId, SupplierPaths.parse(supplier), dto, Long.parseLong(userId)));
    }

    /** status: SUGGESTED, CONFIRMED, REJECTED, NOT_SUPPLIED or UNLINKED (default). */
    @GetMapping("/links")
    public ResponseEntity<List<SupplierLinkResponse>> listLinks(@PathVariable Long storeId, @PathVariable String supplier,
                                                                @RequestParam(defaultValue = "UNLINKED") String status,
                                                                @AuthenticationPrincipal String userId) {
        return ResponseEntity.ok(service.listLinks(storeId, SupplierPaths.parse(supplier), status, Long.parseLong(userId)));
    }

    @PostMapping("/links/confirm")
    public ResponseEntity<List<SupplierLinkResponse>> confirmLinks(@PathVariable Long storeId, @PathVariable String supplier,
                                                                   @Valid @RequestBody ConfirmLinksDto dto,
                                                                   @AuthenticationPrincipal String userId) {
        return ResponseEntity.ok(service.confirmLinks(storeId, SupplierPaths.parse(supplier), dto.linkIds(), Long.parseLong(userId)));
    }

    @PostMapping("/links/{linkId}/reject")
    public ResponseEntity<Void> rejectLink(@PathVariable Long storeId, @PathVariable String supplier,
                                           @PathVariable Long linkId, @AuthenticationPrincipal String userId) {
        service.rejectLink(storeId, SupplierPaths.parse(supplier), linkId, Long.parseLong(userId));
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/variants/{variantId}/link")
    public ResponseEntity<SupplierLinkResponse> linkManually(@PathVariable Long storeId, @PathVariable String supplier,
                                                             @PathVariable Long variantId,
                                                             @Valid @RequestBody ManualLinkDto dto,
                                                             @AuthenticationPrincipal String userId) {
        return ResponseEntity.ok(service.linkManually(storeId, SupplierPaths.parse(supplier), variantId,
                dto.supplierItemId(), Long.parseLong(userId)));
    }

    @PutMapping("/variants/{variantId}/not-supplied")
    public ResponseEntity<Void> markNotSupplied(@PathVariable Long storeId, @PathVariable String supplier,
                                                @PathVariable Long variantId, @AuthenticationPrincipal String userId) {
        service.markNotSupplied(storeId, SupplierPaths.parse(supplier), variantId, Long.parseLong(userId));
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/variants/{variantId}/link")
    public ResponseEntity<Void> unlink(@PathVariable Long storeId, @PathVariable String supplier,
                                       @PathVariable Long variantId, @AuthenticationPrincipal String userId) {
        service.unlink(storeId, SupplierPaths.parse(supplier), variantId, Long.parseLong(userId));
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/items")
    public ResponseEntity<List<SupplierItemResponse>> searchItems(@PathVariable Long storeId, @PathVariable String supplier,
                                                                  @RequestParam(defaultValue = "") String q,
                                                                  @AuthenticationPrincipal String userId) {
        return ResponseEntity.ok(service.searchItems(storeId, SupplierPaths.parse(supplier), q, Long.parseLong(userId)));
    }

    @GetMapping("/runs")
    public ResponseEntity<List<SyncRunResponse>> listRuns(@PathVariable Long storeId, @PathVariable String supplier,
                                                          @RequestParam(defaultValue = "10") int limit,
                                                          @AuthenticationPrincipal String userId) {
        return ResponseEntity.ok(service.listRuns(storeId, SupplierPaths.parse(supplier), limit, Long.parseLong(userId)));
    }

    @GetMapping("/runs/{runId}/events")
    public ResponseEntity<List<SyncEventResponse>> listRunEvents(@PathVariable Long storeId, @PathVariable String supplier,
                                                                 @PathVariable Long runId,
                                                                 @AuthenticationPrincipal String userId) {
        SupplierPaths.parse(supplier);
        return ResponseEntity.ok(service.listRunEvents(storeId, runId, Long.parseLong(userId)));
    }
}
