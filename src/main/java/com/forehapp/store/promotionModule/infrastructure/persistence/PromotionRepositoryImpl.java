package com.forehapp.store.promotionModule.infrastructure.persistence;

import com.forehapp.store.promotionModule.domain.model.Coupon;
import com.forehapp.store.promotionModule.domain.model.CouponRedemption;
import com.forehapp.store.promotionModule.domain.ports.out.ICouponDao;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class PromotionRepositoryImpl implements ICouponDao {

    private final ICouponRepository couponRepository;
    private final ICouponRedemptionRepository redemptionRepository;

    public PromotionRepositoryImpl(ICouponRepository couponRepository,
                                   ICouponRedemptionRepository redemptionRepository) {
        this.couponRepository = couponRepository;
        this.redemptionRepository = redemptionRepository;
    }

    @Override
    public Optional<Coupon> findById(Long id) {
        return couponRepository.findById(id);
    }

    @Override
    public Optional<Coupon> findByCode(String code) {
        return couponRepository.findByCode(code);
    }

    @Override
    public Page<Coupon> findAll(Pageable pageable) {
        return couponRepository.findAll(pageable);
    }

    @Override
    public Page<Coupon> findBySellerId(Long sellerId, Pageable pageable) {
        return couponRepository.findBySellerId(sellerId, pageable);
    }

    @Override
    public Coupon save(Coupon coupon) {
        return couponRepository.save(coupon);
    }

    @Override
    public void delete(Coupon coupon) {
        couponRepository.delete(coupon);
    }

    @Override
    public long countRedemptionsByCouponId(Long couponId) {
        return redemptionRepository.countByCouponId(couponId);
    }

    @Override
    public long countRedemptionsByCouponIdAndProfileId(Long couponId, Long profileId) {
        return redemptionRepository.countByCouponIdAndProfileId(couponId, profileId);
    }

    @Override
    public CouponRedemption saveRedemption(CouponRedemption redemption) {
        return redemptionRepository.save(redemption);
    }
}
