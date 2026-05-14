package com.forehapp.store.wishlistModule.application.dto;

import jakarta.validation.constraints.NotNull;

public record AddToWishlistDto(@NotNull Long productId) {}
