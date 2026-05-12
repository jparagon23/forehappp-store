package com.forehapp.store.productModule.infrastructure.web;

import com.forehapp.store.productModule.application.dto.BrandResponse;
import com.forehapp.store.productModule.application.dto.CategoryAttributeResponse;
import com.forehapp.store.productModule.application.dto.CategoryResponse;
import com.forehapp.store.productModule.application.dto.LineResponse;
import com.forehapp.store.productModule.domain.ports.in.ICatalogService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class CatalogController {

    private final ICatalogService catalogService;

    public CatalogController(ICatalogService catalogService) {
        this.catalogService = catalogService;
    }

    @GetMapping("/brands")
    public ResponseEntity<List<BrandResponse>> getBrands() {
        return ResponseEntity.ok(catalogService.findAllBrands());
    }

    @GetMapping("/brands/{brandId}/lines")
    public ResponseEntity<List<LineResponse>> getLines(
            @PathVariable Long brandId,
            @RequestParam(required = false) Long categoryId) {
        if (categoryId != null) {
            return ResponseEntity.ok(catalogService.findLinesByBrandAndCategory(brandId, categoryId));
        }
        return ResponseEntity.ok(catalogService.findLinesByBrand(brandId));
    }

    @GetMapping("/categories")
    public ResponseEntity<List<CategoryResponse>> getCategories() {
        return ResponseEntity.ok(catalogService.findAllCategories());
    }

    @GetMapping("/categories/{categoryId}/attributes")
    public ResponseEntity<List<CategoryAttributeResponse>> getCategoryAttributes(
            @PathVariable Long categoryId) {
        return ResponseEntity.ok(catalogService.findCategoryAttributes(categoryId));
    }
}
