package com.forehapp.store.reviewModule.domain.ports.in;

import com.forehapp.store.reviewModule.application.dto.ReviewPageResponse;
import com.forehapp.store.reviewModule.application.dto.ReviewResponse;

public interface IReviewModuleService {
    ReviewPageResponse getPendingReviews(Long userId, int page, int size);
    ReviewResponse approveReview(Long userId, Long reviewId);
    ReviewResponse rejectReview(Long userId, Long reviewId);
}
