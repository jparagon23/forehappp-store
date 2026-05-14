package com.forehapp.store.reviewModule.domain.ports.in;

import com.forehapp.store.reviewModule.application.dto.CreateReviewRequestDto;
import com.forehapp.store.reviewModule.application.dto.ProductRatingSummary;
import com.forehapp.store.reviewModule.application.dto.ReviewPageResponse;
import com.forehapp.store.reviewModule.application.dto.ReviewResponse;

import java.util.List;

public interface IReviewService {
    ReviewPageResponse getProductReviews(Long productId, int page, int size);
    ProductRatingSummary getProductRatingSummary(Long productId);
    ReviewResponse createReview(Long userId, Long productId, CreateReviewRequestDto dto);
    void deleteReview(Long userId, Long reviewId);
    List<ReviewResponse> getMyReviews(Long userId);
}
