package com.forehapp.store.userModule.domain.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "store_profiles")
@Getter @Setter
@NoArgsConstructor
public class StoreProfile {

    @Id
    @Column(name = "store_profile_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "store_profile_roles", joinColumns = @JoinColumn(name = "store_profile_id"))
    @Column(name = "role")
    @Enumerated(EnumType.STRING)
    private Set<StoreRole> roles = new HashSet<>();

    @Column(length = 50)
    private String phone;

    @OneToMany(mappedBy = "storeProfile", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserAddress> addresses = new ArrayList<>();

    @Column(name = "loyalty_points", nullable = false)
    private Integer loyaltyPoints = 0;

    @Column(nullable = false)
    private Boolean active = true;
}
