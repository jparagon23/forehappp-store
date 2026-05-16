package com.forehapp.store.wishlistModule.infrastructure.persistence;

import com.forehapp.store.wishlistModule.domain.model.Wishlist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface IWishlistRepository extends JpaRepository<Wishlist, Long> {

    // BUG-F: stable ordering — newest items first
    // variants are loaded via @BatchSize on Product.variants to avoid N+1 without multiple-bag conflict
    @Query("SELECT w FROM Wishlist w " +
           "LEFT JOIN FETCH w.items i " +
           "LEFT JOIN FETCH i.product p " +
           "WHERE w.owner.id = :ownerId " +
           "ORDER BY i.addedAt DESC")
    Optional<Wishlist> findByOwnerId(@Param("ownerId") Long ownerId);
}
