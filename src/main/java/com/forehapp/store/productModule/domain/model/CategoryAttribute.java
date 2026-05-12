package com.forehapp.store.productModule.domain.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "store_category_attributes",
        uniqueConstraints = @UniqueConstraint(columnNames = {"category_id", "attribute_id"}))
@Getter @Setter
@NoArgsConstructor
public class CategoryAttribute {

    @Id
    @Column(name = "category_attribute_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attribute_id", nullable = false)
    private Attribute attribute;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    /** "T" if this attribute is required for products in this category, "F" otherwise */
    @Column(nullable = false, length = 1)
    private String required;
}
