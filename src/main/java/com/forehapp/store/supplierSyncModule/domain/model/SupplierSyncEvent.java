package com.forehapp.store.supplierSyncModule.domain.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** One line of a sync run report. Product data is a snapshot so the report survives later edits. */
@Entity
@Table(name = "store_supplier_sync_events")
@Getter @Setter
@NoArgsConstructor
public class SupplierSyncEvent {

    @Id
    @Column(name = "event_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "run_id", nullable = false)
    private Long runId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SyncEventType type;

    @Column(name = "variant_id")
    private Long variantId;

    @Column(name = "product_id")
    private Long productId;

    @Column(name = "product_title", length = 255)
    private String productTitle;

    @Column(name = "variant_label", length = 255)
    private String variantLabel;

    @Column(name = "supplier_item_name", length = 255)
    private String supplierItemName;

    @Column(name = "old_value", length = 100)
    private String oldValue;

    @Column(name = "new_value", length = 100)
    private String newValue;

    @Column(length = 255)
    private String detail;

    /** True when the event comes from a SUGGESTED (not yet confirmed) pair. */
    @Column(nullable = false)
    private Boolean unconfirmed = false;
}
