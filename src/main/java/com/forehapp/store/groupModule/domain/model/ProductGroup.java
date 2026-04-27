package com.forehapp.store.groupModule.domain.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "product_groups")
@Getter @Setter
@NoArgsConstructor
public class ProductGroup {

    @Id
    @Column(name = "group_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String name;

    /** e.g. "BUNDLE", "KIT", "PROMO" */
    @Column(nullable = false, length = 50)
    private String type;

    @Column(precision = 5, scale = 2)
    private BigDecimal discount;
}
