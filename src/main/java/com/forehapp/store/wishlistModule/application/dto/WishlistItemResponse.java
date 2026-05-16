package com.forehapp.store.wishlistModule.application.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record WishlistItemResponse(
        Long itemId,
        Long productId,
        String productTitle,
        String thumbnailUrl,
        BigDecimal minPrice,
        Integer variantCount,
        LocalDateTime addedAt
) {}
