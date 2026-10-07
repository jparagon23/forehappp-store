package com.forehapp.store.orderModule.domain.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * One line of a seller edit to the products of an order. Product names are copied so the history
 * reads the same even if the catalog changes later. All lines of one edit share editId.
 */
@Entity
@Table(name = "store_order_item_changes")
@Getter @Setter
@NoArgsConstructor
public class OrderItemChange {

    @Id
    @Column(name = "change_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "edit_id", nullable = false, length = 36)
    private String editId;

    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @Column(name = "group_id", nullable = false)
    private Long groupId;

    @Enumerated(EnumType.STRING)
    @Column(name = "change_type", nullable = false, length = 20)
    private OrderItemChangeType type;

    @Column(name = "old_variant_id")
    private Long oldVariantId;

    @Column(name = "old_label", length = 400)
    private String oldLabel;

    @Column(name = "old_quantity")
    private Integer oldQuantity;

    @Column(name = "old_unit_price", precision = 14, scale = 2)
    private BigDecimal oldUnitPrice;

    @Column(name = "new_variant_id")
    private Long newVariantId;

    @Column(name = "new_label", length = 400)
    private String newLabel;

    @Column(name = "new_quantity")
    private Integer newQuantity;

    @Column(name = "new_unit_price", precision = 14, scale = 2)
    private BigDecimal newUnitPrice;

    @Column(nullable = false, length = 500)
    private String reason;

    @Column(name = "order_total_before", nullable = false, precision = 14, scale = 2)
    private BigDecimal orderTotalBefore;

    @Column(name = "order_total_after", nullable = false, precision = 14, scale = 2)
    private BigDecimal orderTotalAfter;

    @Column(name = "changed_by_user_id", nullable = false)
    private Long changedByUserId;

    @Column(name = "changed_at", nullable = false)
    private LocalDateTime changedAt;

    @PrePersist
    public void prePersist() {
        if (changedAt == null) changedAt = LocalDateTime.now();
    }
}
