package com.forehapp.store.catalogRequestModule.infrastructure.web;

import com.forehapp.store.catalogRequestModule.application.dto.CatalogRequestResponse;
import com.forehapp.store.catalogRequestModule.application.dto.CreateCatalogRequestDto;
import com.forehapp.store.catalogRequestModule.domain.model.CatalogRequestStatus;
import com.forehapp.store.catalogRequestModule.domain.ports.in.ICatalogRequestService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/stores/{storeId}/catalog-requests")
public class SellerCatalogRequestController {

    private final ICatalogRequestService catalogRequestService;

    public SellerCatalogRequestController(ICatalogRequestService catalogRequestService) {
        this.catalogRequestService = catalogRequestService;
    }

    @PostMapping
    public ResponseEntity<CatalogRequestResponse> create(
            @PathVariable Long storeId,
            @Valid @RequestBody CreateCatalogRequestDto dto,
            @AuthenticationPrincipal String userId) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(catalogRequestService.create(storeId, Long.parseLong(userId), dto));
    }

    @GetMapping
    public ResponseEntity<List<CatalogRequestResponse>> findByStore(
            @PathVariable Long storeId,
            @RequestParam(required = false) CatalogRequestStatus status,
            @AuthenticationPrincipal String userId) {
        return ResponseEntity.ok(catalogRequestService.findByStore(storeId, Long.parseLong(userId), status));
    }

    @DeleteMapping("/{requestId}")
    public ResponseEntity<Void> cancel(
            @PathVariable Long storeId,
            @PathVariable Long requestId,
            @AuthenticationPrincipal String userId) {
        catalogRequestService.cancel(storeId, requestId, Long.parseLong(userId));
        return ResponseEntity.noContent().build();
    }
}
