package com.forehapp.store.productModule.infrastructure.web;

import com.forehapp.store.productModule.application.dto.PublicProductDetailResponse;
import com.forehapp.store.productModule.application.dto.PublicProductSummaryResponse;
import com.forehapp.store.productModule.domain.ports.in.IPublicProductService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/products/public")
public class PublicProductController {

    private final IPublicProductService publicProductService;

    public PublicProductController(IPublicProductService publicProductService) {
        this.publicProductService = publicProductService;
    }

    @GetMapping
    public ResponseEntity<Page<PublicProductSummaryResponse>> listProducts(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long brandId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        size = Math.min(size, 50);
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return ResponseEntity.ok(publicProductService.findActiveProducts(search, categoryId, brandId, pageable));
    }

    @GetMapping("/{productId}")
    public ResponseEntity<PublicProductDetailResponse> getProduct(@PathVariable Long productId) {
        return ResponseEntity.ok(publicProductService.findActiveProductById(productId));
    }
}
