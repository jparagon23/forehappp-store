package com.forehapp.store.promotionModule.infrastructure.web;

import com.forehapp.store.promotionModule.application.dto.CouponResponse;
import com.forehapp.store.promotionModule.application.dto.CreateCouponRequestDto;
import com.forehapp.store.promotionModule.application.dto.UpdateCouponRequestDto;
import com.forehapp.store.promotionModule.domain.ports.in.IPromotionModuleService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/stores/{storeId}/coupons")
public class SellerCouponController {

    private final IPromotionModuleService promotionModuleService;

    public SellerCouponController(IPromotionModuleService promotionModuleService) {
        this.promotionModuleService = promotionModuleService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CouponResponse createCoupon(
            @PathVariable Long storeId,
            @AuthenticationPrincipal String userId,
            @Valid @RequestBody CreateCouponRequestDto dto) {
        return promotionModuleService.createCoupon(storeId, Long.parseLong(userId), dto);
    }

    @GetMapping
    public Page<CouponResponse> listMyCoupons(
            @PathVariable Long storeId,
            @AuthenticationPrincipal String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return promotionModuleService.listMyCoupons(storeId, Long.parseLong(userId), page, size);
    }

    @GetMapping("/{couponId}")
    public CouponResponse getCoupon(
            @PathVariable Long storeId,
            @AuthenticationPrincipal String userId,
            @PathVariable Long couponId) {
        return promotionModuleService.getCoupon(storeId, Long.parseLong(userId), couponId);
    }

    @PatchMapping("/{couponId}")
    public CouponResponse updateCoupon(
            @PathVariable Long storeId,
            @AuthenticationPrincipal String userId,
            @PathVariable Long couponId,
            @Valid @RequestBody UpdateCouponRequestDto dto) {
        return promotionModuleService.updateCoupon(storeId, Long.parseLong(userId), couponId, dto);
    }

    @PatchMapping("/{couponId}/deactivate")
    public CouponResponse deactivateCoupon(
            @PathVariable Long storeId,
            @AuthenticationPrincipal String userId,
            @PathVariable Long couponId) {
        return promotionModuleService.deactivateCoupon(storeId, Long.parseLong(userId), couponId);
    }
}
