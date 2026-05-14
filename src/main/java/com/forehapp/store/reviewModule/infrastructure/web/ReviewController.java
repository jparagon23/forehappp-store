package com.forehapp.store.reviewModule.infrastructure.web;

import com.forehapp.store.reviewModule.application.dto.CreateReviewRequestDto;
import com.forehapp.store.reviewModule.application.dto.ProductRatingSummary;
import com.forehapp.store.reviewModule.application.dto.ReviewPageResponse;
import com.forehapp.store.reviewModule.application.dto.ReviewResponse;
import com.forehapp.store.reviewModule.domain.ports.in.IReviewService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class ReviewController {

    private final IReviewService reviewService;

    public ReviewController(IReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @GetMapping("/products/{productId}/reviews")
    public ReviewPageResponse getProductReviews(
            @PathVariable Long productId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return reviewService.getProductReviews(productId, page, size);
    }

    @GetMapping("/products/{productId}/reviews/summary")
    public ProductRatingSummary getProductRatingSummary(@PathVariable Long productId) {
        return reviewService.getProductRatingSummary(productId);
    }

    @PostMapping("/products/{productId}/reviews")
    @ResponseStatus(HttpStatus.CREATED)
    public ReviewResponse createReview(
            @AuthenticationPrincipal String userId,
            @PathVariable Long productId,
            @Valid @RequestBody CreateReviewRequestDto dto) {
        return reviewService.createReview(Long.parseLong(userId), productId, dto);
    }

    @DeleteMapping("/reviews/{reviewId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteReview(
            @AuthenticationPrincipal String userId,
            @PathVariable Long reviewId) {
        reviewService.deleteReview(Long.parseLong(userId), reviewId);
    }

    @GetMapping("/reviews/my")
    public List<ReviewResponse> getMyReviews(@AuthenticationPrincipal String userId) {
        return reviewService.getMyReviews(Long.parseLong(userId));
    }
}
