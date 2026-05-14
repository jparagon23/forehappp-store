package com.forehapp.store.reviewModule.infrastructure.web;

import com.forehapp.store.reviewModule.application.dto.ReviewPageResponse;
import com.forehapp.store.reviewModule.application.dto.ReviewResponse;
import com.forehapp.store.reviewModule.domain.ports.in.IReviewModuleService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/reviews")
public class ReviewAdminController {

    private final IReviewModuleService reviewModuleService;

    public ReviewAdminController(IReviewModuleService reviewModuleService) {
        this.reviewModuleService = reviewModuleService;
    }

    @GetMapping("/pending")
    public ReviewPageResponse getPendingReviews(
            @AuthenticationPrincipal String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return reviewModuleService.getPendingReviews(Long.parseLong(userId), page, size);
    }

    @PatchMapping("/{reviewId}/approve")
    public ReviewResponse approveReview(
            @AuthenticationPrincipal String userId,
            @PathVariable Long reviewId) {
        return reviewModuleService.approveReview(Long.parseLong(userId), reviewId);
    }

    @PatchMapping("/{reviewId}/reject")
    public ReviewResponse rejectReview(
            @AuthenticationPrincipal String userId,
            @PathVariable Long reviewId) {
        return reviewModuleService.rejectReview(Long.parseLong(userId), reviewId);
    }
}
