package com.forehapp.store.productModule.infrastructure.web;

import com.forehapp.store.productModule.application.dto.*;
import com.forehapp.store.productModule.domain.ports.in.IAdminCatalogService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin")
public class AdminCatalogController {

    private final IAdminCatalogService adminCatalogService;

    public AdminCatalogController(IAdminCatalogService adminCatalogService) {
        this.adminCatalogService = adminCatalogService;
    }

    @PostMapping("/brands")
    public ResponseEntity<BrandResponse> createBrand(
            @Valid @RequestBody CreateBrandRequestDto dto,
            @AuthenticationPrincipal String userId) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(adminCatalogService.createBrand(dto, Long.parseLong(userId)));
    }

    @PostMapping("/brands/{brandId}/lines")
    public ResponseEntity<LineResponse> createLine(
            @PathVariable Long brandId,
            @Valid @RequestBody CreateLineRequestDto dto,
            @AuthenticationPrincipal String userId) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(adminCatalogService.createLine(brandId, dto, Long.parseLong(userId)));
    }

    @PostMapping("/categories")
    public ResponseEntity<CategoryResponse> createCategory(
            @Valid @RequestBody CreateCategoryRequestDto dto,
            @AuthenticationPrincipal String userId) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(adminCatalogService.createCategory(dto, Long.parseLong(userId)));
    }

    @PostMapping("/attributes")
    public ResponseEntity<AttributeResponse> createAttribute(
            @Valid @RequestBody CreateAttributeRequestDto dto,
            @AuthenticationPrincipal String userId) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(adminCatalogService.createAttribute(dto, Long.parseLong(userId)));
    }

    @PostMapping("/attributes/{attributeId}/values")
    public ResponseEntity<CategoryAttributeResponse.AttributeValueDto> createAttributeValue(
            @PathVariable Long attributeId,
            @Valid @RequestBody CreateAttributeValueRequestDto dto,
            @AuthenticationPrincipal String userId) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(adminCatalogService.createAttributeValue(attributeId, dto, Long.parseLong(userId)));
    }

    @PostMapping("/categories/{categoryId}/attributes")
    public ResponseEntity<CategoryAttributeResponse> linkAttributeToCategory(
            @PathVariable Long categoryId,
            @Valid @RequestBody LinkCategoryAttributeRequestDto dto,
            @AuthenticationPrincipal String userId) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(adminCatalogService.linkAttributeToCategory(categoryId, dto, Long.parseLong(userId)));
    }
}
