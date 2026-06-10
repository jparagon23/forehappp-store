package com.forehapp.store.productModule.domain.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "store_product_tags",
       uniqueConstraints = @UniqueConstraint(name = "uk_product_tag", columnNames = {"product_id", "tag"}))
@Getter @Setter @NoArgsConstructor
public class ProductTag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "tag", length = 50, nullable = false)
    private String tag;
}
