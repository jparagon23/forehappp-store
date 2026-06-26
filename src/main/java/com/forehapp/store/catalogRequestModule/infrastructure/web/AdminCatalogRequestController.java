package com.forehapp.store.catalogRequestModule.infrastructure.web;

import com.forehapp.store.catalogRequestModule.application.dto.CatalogRequestResponse;
import com.forehapp.store.catalogRequestModule.application.dto.RejectCatalogRequestDto;
import com.forehapp.store.catalogRequestModule.domain.model.CatalogRequestStatus;
import com.forehapp.store.catalogRequestModule.domain.model.CatalogRequestType;
import com.forehapp.store.catalogRequestModule.domain.ports.in.IAdminCatalogRequestService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/catalog-requests")
public class AdminCatalogRequestController {

    private final IAdminCatalogRequestService adminCatalogRequestService;

    public AdminCatalogRequestController(IAdminCatalogRequestService adminCatalogRequestService) {
        this.adminCatalogRequestService = adminCatalogRequestService;
    }

    @GetMapping
    public ResponseEntity<List<CatalogRequestResponse>> findAll(
            @RequestParam(required = false) CatalogRequestStatus status,
            @RequestParam(required = false) CatalogRequestType type,
            @AuthenticationPrincipal String userId) {
        return ResponseEntity.ok(adminCatalogRequestService.findAll(Long.parseLong(userId), status, type));
    }

    @PatchMapping("/{requestId}/approve")
    public ResponseEntity<CatalogRequestResponse> approve(
            @PathVariable Long requestId,
            @AuthenticationPrincipal String userId) {
        return ResponseEntity.ok(adminCatalogRequestService.approve(Long.parseLong(userId), requestId));
    }

    @PatchMapping("/{requestId}/reject")
    public ResponseEntity<CatalogRequestResponse> reject(
            @PathVariable Long requestId,
            @Valid @RequestBody RejectCatalogRequestDto dto,
            @AuthenticationPrincipal String userId) {
        return ResponseEntity.ok(adminCatalogRequestService.reject(Long.parseLong(userId), requestId, dto));
    }
}
