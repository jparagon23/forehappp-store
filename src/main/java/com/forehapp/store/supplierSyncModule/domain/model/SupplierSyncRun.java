package com.forehapp.store.supplierSyncModule.domain.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/** One sync execution for one store. */
@Entity
@Table(name = "store_supplier_sync_runs")
@Getter @Setter
@NoArgsConstructor
public class SupplierSyncRun {

    @Id
    @Column(name = "run_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "store_id", nullable = false)
    private Long storeId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private SupplierCode supplier;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SyncMode mode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SyncRunStatus status;

    @Column(name = "abort_reason", length = 500)
    private String abortReason;

    @Column(name = "supplier_items", nullable = false)
    private Integer supplierItems = 0;

    @Column(name = "supplier_out_of_stock", nullable = false)
    private Integer supplierOutOfStock = 0;

    @Column(name = "confirmed_links", nullable = false)
    private Integer confirmedLinks = 0;

    @Column(name = "disabled_count", nullable = false)
    private Integer disabledCount = 0;

    @Column(name = "reenabled_count", nullable = false)
    private Integer reenabledCount = 0;

    @Column(name = "cost_updates", nullable = false)
    private Integer costUpdates = 0;

    @Column(name = "broken_links", nullable = false)
    private Integer brokenLinks = 0;

    @Column(name = "margin_alerts", nullable = false)
    private Integer marginAlerts = 0;

    @Column(name = "orders_at_risk", nullable = false)
    private Integer ordersAtRisk = 0;

    @Column(name = "started_at", nullable = false)
    private LocalDateTime startedAt;

    @Column(name = "finished_at")
    private LocalDateTime finishedAt;
}
