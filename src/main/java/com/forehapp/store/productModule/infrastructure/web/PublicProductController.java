package com.forehapp.store.productModule.infrastructure.web;

import com.forehapp.store.productModule.application.dto.BrandFacetResponse;
import com.forehapp.store.productModule.application.dto.CategoryDiscoverySectionResponse;
import com.forehapp.store.productModule.application.dto.ProductFacetsResponse;
import com.forehapp.store.productModule.application.dto.ProductListingResponse;
import com.forehapp.store.productModule.application.dto.PublicProductDetailResponse;
import com.forehapp.store.productModule.application.dto.PublicProductSummaryResponse;
import com.forehapp.store.productModule.domain.model.ProductSortBy;
import com.forehapp.store.productModule.domain.ports.in.IPublicProductService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/products/public")
public class PublicProductController {

    private final IPublicProductService publicProductService;

    public PublicProductController(IPublicProductService publicProductService) {
        this.publicProductService = publicProductService;
    }

    @GetMapping
    public ResponseEntity<ProductListingResponse> listProducts(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long brandId,
            @RequestParam(required = false) Boolean freeShipping,
            @RequestParam(defaultValue = "NEWEST") String sortBy,
            @RequestParam(defaultValue = "0") String page,
            @RequestParam(defaultValue = "20") String size) {

        int pageNum = parseIntSafe(page, 0);
        int pageSize = Math.min(parseIntSafe(size, 20), 50);
        ProductSortBy sort = parseSortBy(sortBy);

        Pageable pageable = sort == ProductSortBy.NEWEST
                ? PageRequest.of(pageNum, pageSize, Sort.by(Sort.Direction.DESC, "createdAt"))
                : PageRequest.of(pageNum, pageSize);

        Page<PublicProductSummaryResponse> productsPage =
                publicProductService.findActiveProducts(search, categoryId, brandId, freeShipping, sort, pageable);

        ProductFacetsResponse facets = null;
        if (search != null || categoryId != null) {
            List<BrandFacetResponse> brandFacets =
                    publicProductService.findBrandFacets(search, categoryId, freeShipping);
            facets = new ProductFacetsResponse(brandFacets);
        }

        return ResponseEntity.ok(new ProductListingResponse(productsPage, facets));
    }

    private int parseIntSafe(String value, int defaultValue) {
        try {
            int parsed = Integer.parseInt(value);
            return parsed < 0 ? defaultValue : parsed;
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private ProductSortBy parseSortBy(String value) {
        try {
            return ProductSortBy.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            return ProductSortBy.NEWEST;
        }
    }

    @GetMapping("/discovery/sections")
    public ResponseEntity<List<CategoryDiscoverySectionResponse>> getDiscoverySections(
            @RequestParam(defaultValue = "8") String limit) {
        int sectionLimit = Math.min(Math.max(parseIntSafe(limit, 8), 1), 20);
        return ResponseEntity.ok(publicProductService.findDiscoverySections(sectionLimit));
    }

    @GetMapping("/{productId}")
    public ResponseEntity<PublicProductDetailResponse> getProduct(@PathVariable Long productId) {
        return ResponseEntity.ok(publicProductService.findActiveProductById(productId));
    }
}
