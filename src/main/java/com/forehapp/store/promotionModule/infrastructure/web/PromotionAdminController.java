package com.forehapp.store.promotionModule.infrastructure.web;

import com.forehapp.store.promotionModule.application.dto.CouponResponse;
import com.forehapp.store.promotionModule.application.dto.CreateCouponRequestDto;
import com.forehapp.store.promotionModule.application.dto.UpdateCouponRequestDto;
import com.forehapp.store.promotionModule.domain.ports.in.IPromotionModuleService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/coupons")
public class PromotionAdminController {

    private final IPromotionModuleService promotionModuleService;

    public PromotionAdminController(IPromotionModuleService promotionModuleService) {
        this.promotionModuleService = promotionModuleService;
    }

    @GetMapping
    public Page<CouponResponse> listCoupons(
            @AuthenticationPrincipal String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return promotionModuleService.listCoupons(Long.parseLong(userId), page, size);
    }

    @GetMapping("/{couponId}")
    public CouponResponse getCoupon(
            @AuthenticationPrincipal String userId,
            @PathVariable Long couponId) {
        return promotionModuleService.getCoupon(Long.parseLong(userId), couponId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CouponResponse createCoupon(
            @AuthenticationPrincipal String userId,
            @Valid @RequestBody CreateCouponRequestDto dto) {
        return promotionModuleService.createCoupon(Long.parseLong(userId), dto);
    }

    @PatchMapping("/{couponId}")
    public CouponResponse updateCoupon(
            @AuthenticationPrincipal String userId,
            @PathVariable Long couponId,
            @Valid @RequestBody UpdateCouponRequestDto dto) {
        return promotionModuleService.updateCoupon(Long.parseLong(userId), couponId, dto);
    }

    @PatchMapping("/{couponId}/deactivate")
    public CouponResponse deactivateCoupon(
            @AuthenticationPrincipal String userId,
            @PathVariable Long couponId) {
        return promotionModuleService.deactivateCoupon(Long.parseLong(userId), couponId);
    }
}
