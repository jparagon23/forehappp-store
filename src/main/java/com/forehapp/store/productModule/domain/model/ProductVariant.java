package com.forehapp.store.productModule.domain.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.BatchSize;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "store_product_variants")
@Getter @Setter
@NoArgsConstructor
public class ProductVariant {

    @Id
    @Column(name = "variant_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(unique = true, length = 100)
    private String sku;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal price;

    @Column(name = "compare_at_price", precision = 14, scale = 2)
    private BigDecimal compareAtPrice;

    @Column(precision = 14, scale = 2)
    private BigDecimal cost;

    // Units the store physically holds
    @Column(nullable = false)
    private Integer stock = 0;

    // Units beyond the own stock can be sold and shipped by the supplier
    @Column(nullable = false)
    private Boolean dropship = false;

    // Whether the supplier has it right now; only matters when dropship. Kept by the supplier sync
    // for linked variants, set by hand otherwise
    @Column(name = "supplier_available", nullable = false)
    private Boolean supplierAvailable = true;

    @Column(nullable = false)
    private Boolean active = true;

    // Overrides Product.repurchaseDays for this variant (e.g. larger packs); null = inherit
    @Column(name = "repurchase_days")
    private Integer repurchaseDays;

    @BatchSize(size = 25)
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "store_product_variant_attribute_values",
            joinColumns = @JoinColumn(name = "variant_id"),
            inverseJoinColumns = @JoinColumn(name = "attribute_value_id")
    )
    private List<AttributeValue> attributeValues = new ArrayList<>();

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
    }

    /** The supplier can ship it right now, so there is no limit on units. */
    public boolean isDropshipAvailable() {
        return Boolean.TRUE.equals(dropship) && Boolean.TRUE.equals(supplierAvailable);
    }

    /** Active and with own stock or supplier availability. */
    public boolean isSellable() {
        return Boolean.TRUE.equals(active) && (stock > 0 || isDropshipAvailable());
    }

    public boolean canFulfill(int quantity) {
        return isDropshipAvailable() || stock >= quantity;
    }

    /** Most units a buyer can order now; null when the supplier covers any quantity. */
    public Integer maxQuantity() {
        return isDropshipAvailable() ? null : Math.max(stock, 0);
    }

    /**
     * Takes own stock first and the rest from the supplier.
     * @return units to order from the supplier
     */
    public int consume(int quantity) {
        if (!canFulfill(quantity)) {
            throw new IllegalStateException("Variant " + id + " cannot fulfill " + quantity + " units");
        }
        int own = Math.min(Math.max(stock, 0), quantity);
        stock -= own;
        return quantity - own;
    }
}
