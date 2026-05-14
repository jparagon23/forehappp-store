package com.forehapp.store.wishlistModule.domain.ports.in;

import com.forehapp.store.wishlistModule.application.dto.AddToWishlistDto;
import com.forehapp.store.wishlistModule.application.dto.WishlistResponse;

public interface IWishlistService {
    WishlistResponse getWishlist(Long userId);
    WishlistResponse addItem(Long userId, AddToWishlistDto dto);
    WishlistResponse removeItem(Long userId, Long itemId);
    void clearWishlist(Long userId);
}
