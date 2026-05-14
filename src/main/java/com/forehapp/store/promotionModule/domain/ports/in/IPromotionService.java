package com.forehapp.store.promotionModule.domain.ports.in;

import com.forehapp.store.promotionModule.application.dto.CouponValidationResponse;
import com.forehapp.store.promotionModule.application.dto.RedeemCouponRequestDto;
import com.forehapp.store.promotionModule.application.dto.ValidateCouponRequestDto;

public interface IPromotionService {
    CouponValidationResponse validateCoupon(Long userId, ValidateCouponRequestDto dto);
    CouponValidationResponse redeemCoupon(Long userId, RedeemCouponRequestDto dto);
}
