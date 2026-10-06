package com.forehapp.store.supplierSyncModule.domain.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Pairs a store variant with a supplier product, plus the sync state of that variant. */
@Entity
@Table(name = "store_supplier_links")
@Getter @Setter
@NoArgsConstructor
public class SupplierLink {

    @Id
    @Column(name = "link_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "store_id", nullable = false)
    private Long storeId;

    @Column(name = "variant_id", nullable = false)
    private Long variantId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private SupplierCode supplier;

    /** Null only for NOT_SUPPLIED. */
    @Column(name = "supplier_item_id")
    private Long supplierItemId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SupplierLinkStatus status;

    /** Similarity reported by the matching script (0–1); null for pairs set by hand. */
    @Column(precision = 5, scale = 4)
    private BigDecimal score;

    /** True while the sync holds this variant at stock 0 because the supplier is out of stock. */
    @Column(name = "disabled_by_sync", nullable = false)
    private Boolean disabledBySync = false;

    /** Stock the variant had when the sync disabled it, restored when the supplier has it again. */
    @Column(name = "stock_before_sync")
    private Integer stockBeforeSync;

    @Column(name = "confirmed_by_user_id")
    private Long confirmedByUserId;

    @Column(name = "confirmed_at")
    private LocalDateTime confirmedAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
        updatedAt = createdAt;
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
