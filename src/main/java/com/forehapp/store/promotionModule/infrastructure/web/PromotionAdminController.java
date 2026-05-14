package com.forehapp.store.promotionModule.infrastructure.web;

import com.forehapp.store.promotionModule.application.dto.CouponResponse;
import com.forehapp.store.promotionModule.domain.ports.in.IPromotionModuleService;
import org.springframework.data.domain.Page;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/coupons")
public class PromotionAdminController {

    private final IPromotionModuleService promotionModuleService;

    public PromotionAdminController(IPromotionModuleService promotionModuleService) {
        this.promotionModuleService = promotionModuleService;
    }

    @GetMapping
    public Page<CouponResponse> listAllCoupons(
            @AuthenticationPrincipal String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return promotionModuleService.listAllCoupons(Long.parseLong(userId), page, size);
    }
}
