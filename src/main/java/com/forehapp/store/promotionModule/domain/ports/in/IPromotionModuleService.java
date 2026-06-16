package com.forehapp.store.promotionModule.domain.ports.in;

import com.forehapp.store.promotionModule.application.dto.CouponResponse;
import com.forehapp.store.promotionModule.application.dto.CreateCouponRequestDto;
import com.forehapp.store.promotionModule.application.dto.CreateDonationCouponRequestDto;
import com.forehapp.store.promotionModule.application.dto.UpdateCouponRequestDto;
import org.springframework.data.domain.Page;

public interface IPromotionModuleService {
    CouponResponse createCoupon(Long storeId, Long userId, CreateCouponRequestDto dto);
    CouponResponse updateCoupon(Long storeId, Long userId, Long couponId, UpdateCouponRequestDto dto);
    CouponResponse deactivateCoupon(Long storeId, Long userId, Long couponId);
    Page<CouponResponse> listMyCoupons(Long storeId, Long userId, int page, int size);
    CouponResponse getCoupon(Long storeId, Long userId, Long couponId);
    Page<CouponResponse> listAllCoupons(Long userId, int page, int size);
    CouponResponse adminCreateCoupon(Long adminUserId, Long storeId, CreateCouponRequestDto dto);
    CouponResponse adminCreateDonationCoupon(Long adminUserId, CreateDonationCouponRequestDto dto);
    CouponResponse adminUpdateCoupon(Long adminUserId, Long couponId, UpdateCouponRequestDto dto);
    CouponResponse adminDeactivateCoupon(Long adminUserId, Long couponId);
}
