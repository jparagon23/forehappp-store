package com.forehapp.store.promotionModule.domain.ports.in;

import com.forehapp.store.promotionModule.application.dto.CouponResponse;
import com.forehapp.store.promotionModule.application.dto.CreateCouponRequestDto;
import com.forehapp.store.promotionModule.application.dto.UpdateCouponRequestDto;
import org.springframework.data.domain.Page;

public interface IPromotionModuleService {
    CouponResponse createCoupon(Long userId, CreateCouponRequestDto dto);
    CouponResponse updateCoupon(Long userId, Long couponId, UpdateCouponRequestDto dto);
    CouponResponse deactivateCoupon(Long userId, Long couponId);
    Page<CouponResponse> listMyCoupons(Long userId, int page, int size);
    CouponResponse getCoupon(Long userId, Long couponId);
    Page<CouponResponse> listAllCoupons(Long userId, int page, int size);
}
