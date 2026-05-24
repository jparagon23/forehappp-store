package com.forehapp.store.userModule.domain.model;

import com.forehapp.store.locationModule.domain.model.City;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "store_profile_addresses")
@Getter @Setter
@NoArgsConstructor
public class UserAddress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "address_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_profile_id", nullable = false)
    private StoreProfile storeProfile;

    @Column(length = 100)
    private String alias;

    @Column(nullable = false, length = 255)
    private String street;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "city_id", nullable = false)
    private City city;

    @Column(name = "zip_code", length = 20)
    private String zipCode;

    @Column(name = "is_default", nullable = false)
    private Boolean isDefault = false;
}
