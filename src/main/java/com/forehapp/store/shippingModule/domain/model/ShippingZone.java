package com.forehapp.store.shippingModule.domain.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "shipping_zones")
@Getter @Setter @NoArgsConstructor
public class ShippingZone {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String name;

    @ElementCollection
    @CollectionTable(name = "shipping_zone_cities", joinColumns = @JoinColumn(name = "zone_id"))
    @Column(name = "city", nullable = false, length = 150)
    private List<String> cities = new ArrayList<>();

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal cost;

    @Column(name = "is_default", nullable = false)
    private Boolean isDefault = false;

    @Column(nullable = false)
    private Boolean active = true;
}
