package com.forehapp.store.wishlistModule.infrastructure.persistence;

import com.forehapp.store.wishlistModule.domain.model.Wishlist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface IWishlistRepository extends JpaRepository<Wishlist, Long> {

    // BUG-A: fetch variants in the same query to avoid N+1 per product
    // BUG-F: stable ordering — newest items first
    @Query("SELECT w FROM Wishlist w " +
           "LEFT JOIN FETCH w.items i " +
           "LEFT JOIN FETCH i.product p " +
           "LEFT JOIN FETCH p.variants " +
           "WHERE w.owner.id = :ownerId " +
           "ORDER BY i.addedAt DESC")
    Optional<Wishlist> findByOwnerId(@Param("ownerId") Long ownerId);
}
