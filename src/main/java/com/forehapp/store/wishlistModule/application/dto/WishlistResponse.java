package com.forehapp.store.wishlistModule.application.dto;

import java.util.List;

public record WishlistResponse(
        Long wishlistId,
        Integer totalItems,
        List<WishlistItemResponse> items
) {}
