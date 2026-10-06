package com.forehapp.store.supplierSyncModule.infrastructure.web;

import com.forehapp.store.general.exceptions.ErrorCode;
import com.forehapp.store.general.exceptions.ForbiddenException;
import com.forehapp.store.supplierSyncModule.application.SupplierSyncSettings;
import com.forehapp.store.supplierSyncModule.application.dto.IngestResponse;
import com.forehapp.store.supplierSyncModule.application.dto.SupplierCatalogIngestDto;
import com.forehapp.store.supplierSyncModule.application.dto.VariantForMatchingResponse;
import com.forehapp.store.supplierSyncModule.domain.ports.in.ISupplierSyncIngestService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;

/**
 * Called by the daily scraping job, not by users: open in the security config and authenticated
 * with the shared key in the X-Sync-Key header. Disabled while app.supplier-sync.api-key is empty.
 */
@RestController
@RequestMapping("/api/v1/internal/supplier-sync/{supplier}")
public class SupplierSyncInternalController {

    private static final String KEY_HEADER = "X-Sync-Key";

    private final ISupplierSyncIngestService ingestService;
    private final SupplierSyncSettings settings;

    public SupplierSyncInternalController(ISupplierSyncIngestService ingestService, SupplierSyncSettings settings) {
        this.ingestService = ingestService;
        this.settings = settings;
    }

    @PostMapping("/catalog")
    public ResponseEntity<IngestResponse> ingest(@PathVariable String supplier,
                                                 @RequestHeader(value = KEY_HEADER, required = false) String key,
                                                 @Valid @RequestBody SupplierCatalogIngestDto dto) {
        requireKey(key);
        return ResponseEntity.ok(ingestService.ingest(SupplierPaths.parse(supplier), dto));
    }

    @GetMapping("/variants")
    public ResponseEntity<List<VariantForMatchingResponse>> variantsForMatching(
            @PathVariable String supplier,
            @RequestHeader(value = KEY_HEADER, required = false) String key) {
        requireKey(key);
        return ResponseEntity.ok(ingestService.variantsForMatching(SupplierPaths.parse(supplier)));
    }

    private void requireKey(String key) {
        String expected = settings.getApiKey();
        boolean valid = expected != null && !expected.isBlank() && key != null
                && MessageDigest.isEqual(expected.getBytes(StandardCharsets.UTF_8), key.getBytes(StandardCharsets.UTF_8));
        if (!valid) {
            throw new ForbiddenException(ErrorCode.SUPPLIER_SYNC_KEY_INVALID, "Invalid sync key");
        }
    }
}
