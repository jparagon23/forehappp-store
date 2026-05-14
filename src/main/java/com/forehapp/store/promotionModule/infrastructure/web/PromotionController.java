package com.forehapp.store.promotionModule.infrastructure.web;

import com.forehapp.store.promotionModule.application.dto.CouponValidationResponse;
import com.forehapp.store.promotionModule.application.dto.RedeemCouponRequestDto;
import com.forehapp.store.promotionModule.application.dto.ValidateCouponRequestDto;
import com.forehapp.store.promotionModule.domain.ports.in.IPromotionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/coupons")
public class PromotionController {

    private final IPromotionService promotionService;

    public PromotionController(IPromotionService promotionService) {
        this.promotionService = promotionService;
    }

    @PostMapping("/validate")
    public CouponValidationResponse validateCoupon(
            @AuthenticationPrincipal String userId,
            @Valid @RequestBody ValidateCouponRequestDto dto) {
        return promotionService.validateCoupon(Long.parseLong(userId), dto);
    }

    @PostMapping("/redeem")
    @ResponseStatus(HttpStatus.CREATED)
    public CouponValidationResponse redeemCoupon(
            @AuthenticationPrincipal String userId,
            @Valid @RequestBody RedeemCouponRequestDto dto) {
        return promotionService.redeemCoupon(Long.parseLong(userId), dto);
    }
}
