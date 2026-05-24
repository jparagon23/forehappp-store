package com.forehapp.store.productModule.infrastructure.web;

import com.forehapp.store.productModule.application.dto.CreateProductRequestDto;
import com.forehapp.store.productModule.application.dto.CreateVariantDto;
import com.forehapp.store.productModule.application.dto.ProductImageResponse;
import com.forehapp.store.productModule.application.dto.ProductResponse;
import com.forehapp.store.productModule.application.dto.ProductVariantResponse;
import com.forehapp.store.productModule.application.dto.SellerProductDetailResponse;
import com.forehapp.store.productModule.application.dto.UpdateProductRequestDto;
import com.forehapp.store.productModule.application.dto.UpdateVariantDto;
import com.forehapp.store.productModule.domain.ports.in.IProductImageService;
import com.forehapp.store.productModule.domain.ports.in.IProductService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/stores/{storeId}/products")
public class ProductController {

    private final IProductService productService;
    private final IProductImageService productImageService;

    public ProductController(IProductService productService,
                             IProductImageService productImageService) {
        this.productService = productService;
        this.productImageService = productImageService;
    }

    @PostMapping
    public ResponseEntity<ProductResponse> createProduct(
            @PathVariable Long storeId,
            @Valid @RequestBody CreateProductRequestDto dto,
            @AuthenticationPrincipal String userId) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(productService.createProduct(dto, storeId, Long.parseLong(userId)));
    }

    @GetMapping
    public ResponseEntity<List<ProductResponse>> getStoreProducts(
            @PathVariable Long storeId,
            @AuthenticationPrincipal String userId) {
        return ResponseEntity.ok(productService.getStoreProducts(storeId, Long.parseLong(userId)));
    }

    @GetMapping("/{productId}")
    public ResponseEntity<SellerProductDetailResponse> getProduct(
            @PathVariable Long storeId,
            @PathVariable Long productId,
            @AuthenticationPrincipal String userId) {
        return ResponseEntity.ok(productService.getStoreProductById(productId, storeId, Long.parseLong(userId)));
    }

    @PatchMapping("/{productId}")
    public ResponseEntity<ProductResponse> updateProduct(
            @PathVariable Long storeId,
            @PathVariable Long productId,
            @Valid @RequestBody UpdateProductRequestDto dto,
            @AuthenticationPrincipal String userId) {
        return ResponseEntity.ok(productService.updateProduct(productId, dto, storeId, Long.parseLong(userId)));
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<Void> deleteProduct(
            @PathVariable Long storeId,
            @PathVariable Long productId,
            @AuthenticationPrincipal String userId) {
        productService.deleteProduct(productId, storeId, Long.parseLong(userId));
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{productId}/variants")
    public ResponseEntity<ProductVariantResponse> addVariant(
            @PathVariable Long storeId,
            @PathVariable Long productId,
            @Valid @RequestBody CreateVariantDto dto,
            @AuthenticationPrincipal String userId) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(productService.addVariant(productId, dto, storeId, Long.parseLong(userId)));
    }

    @PatchMapping("/{productId}/variants/{variantId}")
    public ResponseEntity<ProductVariantResponse> updateVariant(
            @PathVariable Long storeId,
            @PathVariable Long productId,
            @PathVariable Long variantId,
            @Valid @RequestBody UpdateVariantDto dto,
            @AuthenticationPrincipal String userId) {
        return ResponseEntity.ok(productService.updateVariant(productId, variantId, dto, storeId, Long.parseLong(userId)));
    }

    @PatchMapping("/{productId}/variants/{variantId}/deactivate")
    public ResponseEntity<ProductVariantResponse> deactivateVariant(
            @PathVariable Long storeId,
            @PathVariable Long productId,
            @PathVariable Long variantId,
            @AuthenticationPrincipal String userId) {
        return ResponseEntity.ok(productService.deactivateVariant(productId, variantId, storeId, Long.parseLong(userId)));
    }

    @PatchMapping("/{productId}/variants/{variantId}/activate")
    public ResponseEntity<ProductVariantResponse> activateVariant(
            @PathVariable Long storeId,
            @PathVariable Long productId,
            @PathVariable Long variantId,
            @AuthenticationPrincipal String userId) {
        return ResponseEntity.ok(productService.activateVariant(productId, variantId, storeId, Long.parseLong(userId)));
    }

    @DeleteMapping("/{productId}/variants/{variantId}")
    public ResponseEntity<Void> deleteVariant(
            @PathVariable Long storeId,
            @PathVariable Long productId,
            @PathVariable Long variantId,
            @AuthenticationPrincipal String userId) {
        productService.deleteVariant(productId, variantId, storeId, Long.parseLong(userId));
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{productId}/publish")
    public ResponseEntity<ProductResponse> publish(
            @PathVariable Long storeId,
            @PathVariable Long productId,
            @AuthenticationPrincipal String userId) {
        return ResponseEntity.ok(productService.publish(productId, storeId, Long.parseLong(userId)));
    }

    @PatchMapping("/{productId}/deactivate")
    public ResponseEntity<ProductResponse> deactivate(
            @PathVariable Long storeId,
            @PathVariable Long productId,
            @AuthenticationPrincipal String userId) {
        return ResponseEntity.ok(productService.deactivate(productId, storeId, Long.parseLong(userId)));
    }

    @PatchMapping("/{productId}/activate")
    public ResponseEntity<ProductResponse> activate(
            @PathVariable Long storeId,
            @PathVariable Long productId,
            @AuthenticationPrincipal String userId) {
        return ResponseEntity.ok(productService.activate(productId, storeId, Long.parseLong(userId)));
    }

    @PostMapping("/{productId}/images")
    public ResponseEntity<ProductImageResponse> uploadImage(
            @PathVariable Long storeId,
            @PathVariable Long productId,
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal String userId) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(productImageService.upload(productId, file, storeId, Long.parseLong(userId)));
    }

    @DeleteMapping("/{productId}/images/{imageId}")
    public ResponseEntity<Void> deleteImage(
            @PathVariable Long storeId,
            @PathVariable Long productId,
            @PathVariable Long imageId,
            @AuthenticationPrincipal String userId) {
        productImageService.delete(productId, imageId, storeId, Long.parseLong(userId));
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{productId}/images")
    public ResponseEntity<List<ProductImageResponse>> getImages(@PathVariable Long productId) {
        return ResponseEntity.ok(productImageService.getByProduct(productId));
    }
}
