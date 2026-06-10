package com.forehapp.store.productModule.infrastructure.web;

import com.forehapp.store.productModule.application.dto.SetTagsRequest;
import com.forehapp.store.productModule.domain.ports.in.IProductTagService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/stores/{storeId}/products/{productId}/tags")
public class ProductTagController {

    private final IProductTagService tagService;

    public ProductTagController(IProductTagService tagService) {
        this.tagService = tagService;
    }

    @GetMapping
    public ResponseEntity<List<String>> getTags(
            @PathVariable Long storeId,
            @PathVariable Long productId,
            @AuthenticationPrincipal String userId) {
        return ResponseEntity.ok(tagService.getTags(productId, storeId, Long.parseLong(userId)));
    }

    @PutMapping
    public ResponseEntity<List<String>> setTags(
            @PathVariable Long storeId,
            @PathVariable Long productId,
            @Valid @RequestBody SetTagsRequest request,
            @AuthenticationPrincipal String userId) {
        return ResponseEntity.ok(tagService.setTags(productId, request.getTags(), storeId, Long.parseLong(userId)));
    }
}
