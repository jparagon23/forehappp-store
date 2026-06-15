package com.forehapp.store.ambassadorModule.domain.model;

import com.forehapp.store.userModule.domain.model.StoreProfile;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "store_ambassadors")
@Getter @Setter
@NoArgsConstructor
public class Ambassador {

    @Id
    @Column(name = "ambassador_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_profile_id", nullable = false, unique = true)
    private StoreProfile storeProfile;

    @Column(name = "referral_code", nullable = false, unique = true, length = 50)
    private String referralCode;

    @Column(name = "commission_percentage", nullable = false, precision = 5, scale = 2)
    private BigDecimal commissionPercentage;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AmbassadorStatus status = AmbassadorStatus.ACTIVE;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "ambassador", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AmbassadorCommission> commissions = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
    }
}
