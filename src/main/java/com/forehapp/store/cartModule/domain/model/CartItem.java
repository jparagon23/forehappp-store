package com.forehapp.store.cartModule.domain.model;

import com.forehapp.store.supplierModule.domain.model.ProductSupplier;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "cart_items")
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
    @JoinColumn(name = "product_supplier_id", nullable = false)
    private ProductSupplier productSupplier;

    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "added_date", nullable = false, columnDefinition = "TIMESTAMP")
    private LocalDateTime addedDate;

    @PrePersist
    public void prePersist() {
        addedDate = LocalDateTime.now();
    }
}
