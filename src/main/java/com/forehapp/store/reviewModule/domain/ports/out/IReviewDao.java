package com.forehapp.store.reviewModule.domain.ports.out;

import com.forehapp.store.reviewModule.domain.model.ProductReview;
import com.forehapp.store.reviewModule.domain.model.ReviewStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface IReviewDao {
    Optional<ProductReview> findById(Long id);
    Optional<ProductReview> findByProductIdAndReviewerId(Long productId, Long reviewerId);
    Page<ProductReview> findByProductIdAndStatus(Long productId, ReviewStatus status, Pageable pageable);
    List<ProductReview> findByReviewerId(Long reviewerId);
    Page<ProductReview> findByStatus(ReviewStatus status, Pageable pageable);
    Double findAverageRatingByProductId(Long productId, ReviewStatus status);
    Long countByProductIdAndStatus(Long productId, ReviewStatus status);
    int autoApproveOlderThan(LocalDateTime threshold);
    ProductReview save(ProductReview review);
    void delete(ProductReview review);
}
