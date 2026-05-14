package com.forehapp.store.wishlistModule.domain.ports.out;

import com.forehapp.store.wishlistModule.domain.model.Wishlist;

import java.util.Optional;

public interface IWishlistDao {
    Optional<Wishlist> findByOwnerId(Long ownerId);
    Wishlist save(Wishlist wishlist);
}
