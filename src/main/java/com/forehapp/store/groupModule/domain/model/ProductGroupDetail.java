package com.forehapp.store.groupModule.domain.model;

import com.forehapp.store.productModule.domain.model.Product;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "product_group_details",
        uniqueConstraints = @UniqueConstraint(columnNames = {"group_id", "product_id"}))
@Getter @Setter
@NoArgsConstructor
public class ProductGroupDetail {

    @Id
    @Column(name = "group_detail_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    private ProductGroup productGroup;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;
}
