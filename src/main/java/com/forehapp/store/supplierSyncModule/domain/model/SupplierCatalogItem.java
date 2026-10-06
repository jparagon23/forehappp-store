package com.forehapp.store.supplierSyncModule.domain.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Latest known state of one supplier product, upserted on every ingest. */
@Entity
@Table(name = "store_supplier_catalog_items")
@Getter @Setter
@NoArgsConstructor
public class SupplierCatalogItem {

    @Id
    @Column(name = "item_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private SupplierCode supplier;

    /** Normalized name (lowercase, no accents or punctuation): the identity of the supplier product. */
    @Column(name = "name_key", nullable = false, length = 255)
    private String nameKey;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(length = 150)
    private String brand;

    @Column(length = 150)
    private String category;

    @Column(precision = 14, scale = 2)
    private BigDecimal price;

    @Column(name = "out_of_stock", nullable = false)
    private Boolean outOfStock = false;

    @Column(name = "first_seen_at", nullable = false)
    private LocalDateTime firstSeenAt;

    @Column(name = "last_seen_at", nullable = false)
    private LocalDateTime lastSeenAt;
}
