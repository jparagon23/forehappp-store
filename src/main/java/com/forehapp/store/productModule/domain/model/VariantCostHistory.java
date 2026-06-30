package com.forehapp.store.productModule.domain.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "store_variant_cost_history")
@Getter @Setter
@NoArgsConstructor
public class VariantCostHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "variant_id", nullable = false)
    private ProductVariant variant;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal cost;

    @Column(length = 255)
    private String notes;

    @Column(name = "changed_at", nullable = false)
    private LocalDateTime changedAt;

    @PrePersist
    public void prePersist() {
        changedAt = LocalDateTime.now();
    }
}
