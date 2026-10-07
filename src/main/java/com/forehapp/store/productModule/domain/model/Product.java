package com.forehapp.store.productModule.domain.model;

import com.forehapp.store.storeModule.domain.model.Store;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.Formula;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "store_products")
@Getter @Setter
@NoArgsConstructor
public class Product {

    @Id
    @Column(name = "product_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "brand_id", nullable = false)
    private Brand brand;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "line_id")
    private Line line;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private ProductStatus status = ProductStatus.ACTIVE;

    @Column(name = "free_shipping", nullable = false)
    private Boolean freeShipping = false;

    // Days one unit lasts before the buyer needs to repurchase; null = no repurchase reminder
    @Column(name = "repurchase_days")
    private Integer repurchaseDays;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Formula("(SELECT COUNT(*) FROM store_product_variants v WHERE v.product_id = product_id)")
    private int variantCount;

    @BatchSize(size = 25)
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<ProductVariant> variants = new ArrayList<>();

    @BatchSize(size = 25)
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("displayOrder ASC")
    private List<ProductImage> images = new ArrayList<>();

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<ProductTag> tags = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
    }

    public boolean hasSellableVariant() {
        return variants.stream().anyMatch(ProductVariant::isSellable);
    }

    /**
     * ACTIVE with nothing to sell becomes OUT_OF_STOCK and back again; DRAFT and INACTIVE are left alone.
     * @return true when the status changed
     */
    public boolean refreshStockStatus() {
        boolean sellable = hasSellableVariant();
        if (!sellable && status == ProductStatus.ACTIVE) {
            status = ProductStatus.OUT_OF_STOCK;
            return true;
        }
        if (sellable && status == ProductStatus.OUT_OF_STOCK) {
            status = ProductStatus.ACTIVE;
            return true;
        }
        return false;
    }
}
