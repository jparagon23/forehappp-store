package com.forehapp.store.wishlistModule.domain.model;

import com.forehapp.store.productModule.domain.model.Product;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "store_wishlist_items", uniqueConstraints = {
        @UniqueConstraint(name = "uk_wishlist_product", columnNames = {"wishlist_id", "product_id"})
})
@Getter @Setter
@NoArgsConstructor
public class WishlistItem {

    @Id
    @Column(name = "wishlist_item_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "wishlist_id", nullable = false)
    private Wishlist wishlist;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "added_at", nullable = false)
    private LocalDateTime addedAt;

    @PrePersist
    public void prePersist() {
        addedAt = LocalDateTime.now();
    }
}
