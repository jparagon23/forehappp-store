package com.forehapp.store.wishlistModule.infrastructure.web;

import com.forehapp.store.wishlistModule.application.dto.AddToWishlistDto;
import com.forehapp.store.wishlistModule.application.dto.WishlistResponse;
import com.forehapp.store.wishlistModule.domain.ports.in.IWishlistService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/wishlist")
public class WishlistController {

    private final IWishlistService wishlistService;

    public WishlistController(IWishlistService wishlistService) {
        this.wishlistService = wishlistService;
    }

    @GetMapping
    public ResponseEntity<WishlistResponse> getWishlist(@AuthenticationPrincipal String userId) {
        return ResponseEntity.ok(wishlistService.getWishlist(Long.parseLong(userId)));
    }

    @PostMapping("/items")
    public ResponseEntity<WishlistResponse> addItem(
            @Valid @RequestBody AddToWishlistDto dto,
            @AuthenticationPrincipal String userId) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(wishlistService.addItem(Long.parseLong(userId), dto));
    }

    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<WishlistResponse> removeItem(
            @PathVariable Long itemId,
            @AuthenticationPrincipal String userId) {
        return ResponseEntity.ok(wishlistService.removeItem(Long.parseLong(userId), itemId));
    }

    @DeleteMapping
    public ResponseEntity<Void> clearWishlist(@AuthenticationPrincipal String userId) {
        wishlistService.clearWishlist(Long.parseLong(userId));
        return ResponseEntity.noContent().build();
    }
}
