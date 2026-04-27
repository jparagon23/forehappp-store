package com.forehapp.store.supplierModule.domain.model;

import com.forehapp.store.productModule.domain.model.Product;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "product_suppliers",
        uniqueConstraints = @UniqueConstraint(columnNames = {"product_id", "supplier_id"}))
@Getter @Setter
@NoArgsConstructor
public class ProductSupplier {

    @Id
    @Column(name = "product_supplier_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id", nullable = false)
    private Supplier supplier;

    @Column(name = "available_quantity", nullable = false)
    private Integer availableQuantity;

    @Column(name = "blocked_quantity", nullable = false)
    private Integer blockedQuantity;

    @Column(name = "sold_quantity", nullable = false)
    private Integer soldQuantity;

    @Version
    @Column(nullable = false)
    private Integer version;
}
