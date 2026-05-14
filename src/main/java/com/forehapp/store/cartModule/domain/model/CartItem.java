package com.forehapp.store.cartModule.domain.model;

import com.forehapp.store.productModule.domain.model.ProductVariant;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "store_cart_items", uniqueConstraints = {
        @UniqueConstraint(name = "uk_cart_variant", columnNames = {"cart_id", "variant_id"})
})
@Getter @Setter
@NoArgsConstructor
public class CartItem {

    @Id
    @Column(name = "cart_item_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id", nullable = false)
    private Cart cart;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "variant_id", nullable = false)
    private ProductVariant variant;

    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "price_at_add", nullable = false, precision = 14, scale = 2)
    private BigDecimal priceAtAdd;

    @Column(name = "added_date", nullable = false)
    private LocalDateTime addedDate;

    @PrePersist
    public void prePersist() {
        addedDate = LocalDateTime.now();
    }
}
