package com.forehapp.store.wishlistModule.infrastructure.persistence;

import com.forehapp.store.wishlistModule.domain.model.Wishlist;
import com.forehapp.store.wishlistModule.domain.ports.out.IWishlistDao;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class WishlistRepositoryImpl implements IWishlistDao {

    private final IWishlistRepository jpaRepository;

    public WishlistRepositoryImpl(IWishlistRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Optional<Wishlist> findByOwnerId(Long ownerId) {
        return jpaRepository.findByOwnerId(ownerId);
    }

    @Override
    public Wishlist save(Wishlist wishlist) {
        return jpaRepository.save(wishlist);
    }
}
