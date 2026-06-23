package com.forehapp.store.productModule.infrastructure.web;

import com.forehapp.store.productModule.application.dto.*;
import com.forehapp.store.productModule.domain.ports.in.IAdminCatalogService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin")
public class AdminCatalogController {

    private final IAdminCatalogService adminCatalogService;

    public AdminCatalogController(IAdminCatalogService adminCatalogService) {
        this.adminCatalogService = adminCatalogService;
    }

    // ── Brand ──────────────────────────────────────────────────────────────────
    // POST   /api/v1/admin/brands             → create brand
    // PATCH  /api/v1/admin/brands/{brandId}   → update brand name
    // DELETE /api/v1/admin/brands/{brandId}   → delete brand (fails if products use it)

    @PostMapping("/brands")
    public ResponseEntity<BrandResponse> createBrand(
            @Valid @RequestBody CreateBrandRequestDto dto,
            @AuthenticationPrincipal String userId) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(adminCatalogService.createBrand(dto, Long.parseLong(userId)));
    }

    @PatchMapping("/brands/{brandId}")
    public ResponseEntity<BrandResponse> updateBrand(
            @PathVariable Long brandId,
            @Valid @RequestBody CreateBrandRequestDto dto,
            @AuthenticationPrincipal String userId) {
        return ResponseEntity.ok(adminCatalogService.updateBrand(brandId, dto, Long.parseLong(userId)));
    }

    @DeleteMapping("/brands/{brandId}")
    public ResponseEntity<Void> deleteBrand(
            @PathVariable Long brandId,
            @AuthenticationPrincipal String userId) {
        adminCatalogService.deleteBrand(brandId, Long.parseLong(userId));
        return ResponseEntity.noContent().build();
    }

    // ── Line ───────────────────────────────────────────────────────────────────
    // POST   /api/v1/admin/brands/{brandId}/lines/{lineId}   → create line
    // PATCH  /api/v1/admin/brands/{brandId}/lines/{lineId}   → update line name
    // DELETE /api/v1/admin/brands/{brandId}/lines/{lineId}   → delete line (fails if products use it)

    @PostMapping("/brands/{brandId}/lines")
    public ResponseEntity<LineResponse> createLine(
            @PathVariable Long brandId,
            @Valid @RequestBody CreateLineRequestDto dto,
            @AuthenticationPrincipal String userId) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(adminCatalogService.createLine(brandId, dto, Long.parseLong(userId)));
    }

    @PatchMapping("/brands/{brandId}/lines/{lineId}")
    public ResponseEntity<LineResponse> updateLine(
            @PathVariable Long brandId,
            @PathVariable Long lineId,
            @Valid @RequestBody UpdateLineRequestDto dto,
            @AuthenticationPrincipal String userId) {
        return ResponseEntity.ok(adminCatalogService.updateLine(brandId, lineId, dto, Long.parseLong(userId)));
    }

    @DeleteMapping("/brands/{brandId}/lines/{lineId}")
    public ResponseEntity<Void> deleteLine(
            @PathVariable Long brandId,
            @PathVariable Long lineId,
            @AuthenticationPrincipal String userId) {
        adminCatalogService.deleteLine(brandId, lineId, Long.parseLong(userId));
        return ResponseEntity.noContent().build();
    }

    // ── Category ───────────────────────────────────────────────────────────────
    // GET    /api/v1/admin/categories                                    → list all categories (with sortOrder)
    // POST   /api/v1/admin/categories                                    → create category
    // PATCH  /api/v1/admin/categories/{categoryId}                       → update category name
    // PATCH  /api/v1/admin/categories/{categoryId}/discovery-order       → update discovery sort order
    // DELETE /api/v1/admin/categories/{categoryId}                       → delete category (fails if products use it)

    @GetMapping("/categories")
    public ResponseEntity<List<CategoryResponse>> listCategories(
            @AuthenticationPrincipal String userId) {
        return ResponseEntity.ok(adminCatalogService.listCategories(Long.parseLong(userId)));
    }

    @PostMapping("/categories")
    public ResponseEntity<CategoryResponse> createCategory(
            @Valid @RequestBody CreateCategoryRequestDto dto,
            @AuthenticationPrincipal String userId) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(adminCatalogService.createCategory(dto, Long.parseLong(userId)));
    }

    @PatchMapping("/categories/{categoryId}")
    public ResponseEntity<CategoryResponse> updateCategory(
            @PathVariable Long categoryId,
            @Valid @RequestBody CreateCategoryRequestDto dto,
            @AuthenticationPrincipal String userId) {
        return ResponseEntity.ok(adminCatalogService.updateCategory(categoryId, dto, Long.parseLong(userId)));
    }

    @PatchMapping("/categories/{categoryId}/discovery-order")
    public ResponseEntity<CategoryResponse> updateCategoryDiscoveryOrder(
            @PathVariable Long categoryId,
            @Valid @RequestBody UpdateCategoryDiscoveryOrderRequestDto dto,
            @AuthenticationPrincipal String userId) {
        return ResponseEntity.ok(adminCatalogService.updateCategoryDiscoveryOrder(categoryId, dto, Long.parseLong(userId)));
    }

    @DeleteMapping("/categories/{categoryId}")
    public ResponseEntity<Void> deleteCategory(
            @PathVariable Long categoryId,
            @AuthenticationPrincipal String userId) {
        adminCatalogService.deleteCategory(categoryId, Long.parseLong(userId));
        return ResponseEntity.noContent().build();
    }

    // ── Attribute ──────────────────────────────────────────────────────────────
    // POST   /api/v1/admin/attributes                    → create attribute
    // PATCH  /api/v1/admin/attributes/{attributeId}      → update attribute name
    // DELETE /api/v1/admin/attributes/{attributeId}      → delete attribute (fails if it has values)

    @PostMapping("/attributes")
    public ResponseEntity<AttributeResponse> createAttribute(
            @Valid @RequestBody CreateAttributeRequestDto dto,
            @AuthenticationPrincipal String userId) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(adminCatalogService.createAttribute(dto, Long.parseLong(userId)));
    }

    @PatchMapping("/attributes/{attributeId}")
    public ResponseEntity<AttributeResponse> updateAttribute(
            @PathVariable Long attributeId,
            @Valid @RequestBody CreateAttributeRequestDto dto,
            @AuthenticationPrincipal String userId) {
        return ResponseEntity.ok(adminCatalogService.updateAttribute(attributeId, dto, Long.parseLong(userId)));
    }

    @DeleteMapping("/attributes/{attributeId}")
    public ResponseEntity<Void> deleteAttribute(
            @PathVariable Long attributeId,
            @AuthenticationPrincipal String userId) {
        adminCatalogService.deleteAttribute(attributeId, Long.parseLong(userId));
        return ResponseEntity.noContent().build();
    }

    // ── AttributeValue ─────────────────────────────────────────────────────────
    // POST   /api/v1/admin/attributes/{attributeId}/values                → create value
    // PATCH  /api/v1/admin/attributes/{attributeId}/values/{valueId}      → update value description
    // DELETE /api/v1/admin/attributes/{attributeId}/values/{valueId}      → delete value (fails if used by variants)

    @PostMapping("/attributes/{attributeId}/values")
    public ResponseEntity<CategoryAttributeResponse.AttributeValueDto> createAttributeValue(
            @PathVariable Long attributeId,
            @Valid @RequestBody CreateAttributeValueRequestDto dto,
            @AuthenticationPrincipal String userId) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(adminCatalogService.createAttributeValue(attributeId, dto, Long.parseLong(userId)));
    }

    @PatchMapping("/attributes/{attributeId}/values/{valueId}")
    public ResponseEntity<CategoryAttributeResponse.AttributeValueDto> updateAttributeValue(
            @PathVariable Long attributeId,
            @PathVariable Long valueId,
            @Valid @RequestBody CreateAttributeValueRequestDto dto,
            @AuthenticationPrincipal String userId) {
        return ResponseEntity.ok(
                adminCatalogService.updateAttributeValue(attributeId, valueId, dto, Long.parseLong(userId)));
    }

    @DeleteMapping("/attributes/{attributeId}/values/{valueId}")
    public ResponseEntity<Void> deleteAttributeValue(
            @PathVariable Long attributeId,
            @PathVariable Long valueId,
            @AuthenticationPrincipal String userId) {
        adminCatalogService.deleteAttributeValue(attributeId, valueId, Long.parseLong(userId));
        return ResponseEntity.noContent().build();
    }

    // ── CategoryAttribute link ─────────────────────────────────────────────────
    // POST   /api/v1/admin/categories/{categoryId}/attributes                          → link attribute to category
    // PATCH  /api/v1/admin/categories/{categoryId}/attributes/{attributeId}            → update required flag
    // DELETE /api/v1/admin/categories/{categoryId}/attributes/{attributeId}            → unlink attribute from category

    @PostMapping("/categories/{categoryId}/attributes")
    public ResponseEntity<CategoryAttributeResponse> linkAttributeToCategory(
            @PathVariable Long categoryId,
            @Valid @RequestBody LinkCategoryAttributeRequestDto dto,
            @AuthenticationPrincipal String userId) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(adminCatalogService.linkAttributeToCategory(categoryId, dto, Long.parseLong(userId)));
    }

    @PatchMapping("/categories/{categoryId}/attributes/{attributeId}")
    public ResponseEntity<CategoryAttributeResponse> updateCategoryAttribute(
            @PathVariable Long categoryId,
            @PathVariable Long attributeId,
            @RequestBody UpdateCategoryAttributeRequestDto dto,
            @AuthenticationPrincipal String userId) {
        return ResponseEntity.ok(
                adminCatalogService.updateCategoryAttribute(categoryId, attributeId, dto, Long.parseLong(userId)));
    }

    @DeleteMapping("/categories/{categoryId}/attributes/{attributeId}")
    public ResponseEntity<Void> unlinkAttributeFromCategory(
            @PathVariable Long categoryId,
            @PathVariable Long attributeId,
            @AuthenticationPrincipal String userId) {
        adminCatalogService.unlinkAttributeFromCategory(categoryId, attributeId, Long.parseLong(userId));
        return ResponseEntity.noContent().build();
    }
}
