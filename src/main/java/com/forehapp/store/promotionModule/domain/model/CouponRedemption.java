package com.forehapp.store.promotionModule.domain.model;

import com.forehapp.store.userModule.domain.model.StoreProfile;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "store_coupon_redemptions")
@Getter @Setter
@NoArgsConstructor
public class CouponRedemption {

    @Id
    @Column(name = "redemption_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coupon_id", nullable = false)
    private Coupon coupon;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_profile_id", nullable = false)
    private StoreProfile profile;

    @Column(name = "order_id")
    private Long orderId;

    @Column(name = "discount_applied", nullable = false, precision = 14, scale = 2)
    private BigDecimal discountApplied;

    @Column(name = "used_at", nullable = false)
    private LocalDateTime usedAt;

    @PrePersist
    public void prePersist() {
        usedAt = LocalDateTime.now();
    }
}
