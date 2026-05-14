package com.forehapp.store.promotionModule.infrastructure.persistence;

import com.forehapp.store.promotionModule.domain.model.CouponRedemption;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ICouponRedemptionRepository extends JpaRepository<CouponRedemption, Long> {
    long countByCouponId(Long couponId);
    long countByCouponIdAndProfileId(Long couponId, Long profileId);
}
