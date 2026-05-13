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
@RequestMapping("/api/v1/products")
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
            @Valid @RequestBody CreateProductRequestDto dto,
            @AuthenticationPrincipal String userId) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(productService.createProduct(dto, Long.parseLong(userId)));
    }

    @PostMapping("/{productId}/images")
    public ResponseEntity<ProductImageResponse> uploadImage(
            @PathVariable Long productId,
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal String userId) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(productImageService.upload(productId, file, Long.parseLong(userId)));
    }

    @DeleteMapping("/{productId}/images/{imageId}")
    public ResponseEntity<Void> deleteImage(
            @PathVariable Long productId,
            @PathVariable Long imageId,
            @AuthenticationPrincipal String userId) {
        productImageService.delete(productId, imageId, Long.parseLong(userId));
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{productId}/images")
    public ResponseEntity<List<ProductImageResponse>> getImages(@PathVariable Long productId) {
        return ResponseEntity.ok(productImageService.getByProduct(productId));
    }

    @GetMapping("/{productId}")
    public ResponseEntity<SellerProductDetailResponse> getProduct(
            @PathVariable Long productId,
            @AuthenticationPrincipal String userId) {
        return ResponseEntity.ok(productService.getSellerProductById(productId, Long.parseLong(userId)));
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<Void> deleteProduct(
            @PathVariable Long productId,
            @AuthenticationPrincipal String userId) {
        productService.deleteProduct(productId, Long.parseLong(userId));
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{productId}")
    public ResponseEntity<ProductResponse> updateProduct(
            @PathVariable Long productId,
            @Valid @RequestBody UpdateProductRequestDto dto,
            @AuthenticationPrincipal String userId) {
        return ResponseEntity.ok(productService.updateProduct(productId, dto, Long.parseLong(userId)));
    }

    @PostMapping("/{productId}/variants")
    public ResponseEntity<ProductVariantResponse> addVariant(
            @PathVariable Long productId,
            @Valid @RequestBody CreateVariantDto dto,
            @AuthenticationPrincipal String userId) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(productService.addVariant(productId, dto, Long.parseLong(userId)));
    }

    @DeleteMapping("/{productId}/variants/{variantId}")
    public ResponseEntity<Void> deleteVariant(
            @PathVariable Long productId,
            @PathVariable Long variantId,
            @AuthenticationPrincipal String userId) {
        productService.deleteVariant(productId, variantId, Long.parseLong(userId));
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{productId}/variants/{variantId}")
    public ResponseEntity<ProductVariantResponse> updateVariant(
            @PathVariable Long productId,
            @PathVariable Long variantId,
            @Valid @RequestBody UpdateVariantDto dto,
            @AuthenticationPrincipal String userId) {
        return ResponseEntity.ok(productService.updateVariant(productId, variantId, dto, Long.parseLong(userId)));
    }

    @PatchMapping("/{productId}/publish")
    public ResponseEntity<ProductResponse> publish(
            @PathVariable Long productId,
            @AuthenticationPrincipal String userId) {
        return ResponseEntity.ok(productService.publish(productId, Long.parseLong(userId)));
    }

    @PatchMapping("/{productId}/deactivate")
    public ResponseEntity<ProductResponse> deactivate(
            @PathVariable Long productId,
            @AuthenticationPrincipal String userId) {
        return ResponseEntity.ok(productService.deactivate(productId, Long.parseLong(userId)));
    }

    @PatchMapping("/{productId}/activate")
    public ResponseEntity<ProductResponse> activate(
            @PathVariable Long productId,
            @AuthenticationPrincipal String userId) {
        return ResponseEntity.ok(productService.activate(productId, Long.parseLong(userId)));
    }

    @GetMapping("/seller")
    public ResponseEntity<List<ProductResponse>> getSellerProducts(
            @AuthenticationPrincipal String userId) {
        return ResponseEntity.ok(productService.getSellerProducts(Long.parseLong(userId)));
    }

}
