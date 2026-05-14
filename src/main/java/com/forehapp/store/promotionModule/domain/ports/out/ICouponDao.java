package com.forehapp.store.promotionModule.domain.ports.out;

import com.forehapp.store.promotionModule.domain.model.Coupon;
import com.forehapp.store.promotionModule.domain.model.CouponRedemption;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface ICouponDao {
    Optional<Coupon> findById(Long id);
    Optional<Coupon> findByCode(String code);
    Page<Coupon> findAll(Pageable pageable);
    Coupon save(Coupon coupon);
    void delete(Coupon coupon);
    long countRedemptionsByCouponId(Long couponId);
    long countRedemptionsByCouponIdAndProfileId(Long couponId, Long profileId);
    CouponRedemption saveRedemption(CouponRedemption redemption);
}
