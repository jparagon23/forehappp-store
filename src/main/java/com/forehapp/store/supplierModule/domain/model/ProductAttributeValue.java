package com.forehapp.store.supplierModule.domain.model;

import com.forehapp.store.productModule.domain.model.AttributeValue;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "product_attribute_values",
        uniqueConstraints = @UniqueConstraint(columnNames = {"product_supplier_id", "attribute_value_id"}))
@Getter @Setter
@NoArgsConstructor
public class ProductAttributeValue {

    @Id
    @Column(name = "product_attribute_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_supplier_id", nullable = false)
    private ProductSupplier productSupplier;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attribute_value_id", nullable = false)
    private AttributeValue attributeValue;
}
