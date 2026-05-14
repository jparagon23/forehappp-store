package com.forehapp.store.reviewModule.infrastructure.persistence;

import com.forehapp.store.reviewModule.domain.model.ProductReview;
import com.forehapp.store.reviewModule.domain.model.ReviewStatus;
import com.forehapp.store.reviewModule.domain.ports.out.IReviewDao;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public class ReviewRepositoryImpl implements IReviewDao {

    private final IReviewRepository repository;

    public ReviewRepositoryImpl(IReviewRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<ProductReview> findById(Long id) {
        return repository.findByIdWithDetails(id);
    }

    @Override
    public Optional<ProductReview> findByProductIdAndReviewerId(Long productId, Long reviewerId) {
        return repository.findByProductIdAndReviewerId(productId, reviewerId);
    }

    @Override
    public Page<ProductReview> findByProductIdAndStatus(Long productId, ReviewStatus status, Pageable pageable) {
        return repository.findByProductIdAndStatus(productId, status, pageable);
    }

    @Override
    public List<ProductReview> findByReviewerId(Long reviewerId) {
        return repository.findByReviewerId(reviewerId);
    }

    @Override
    public Page<ProductReview> findByStatus(ReviewStatus status, Pageable pageable) {
        return repository.findByStatus(status, pageable);
    }

    @Override
    public Double findAverageRatingByProductId(Long productId, ReviewStatus status) {
        return repository.findAverageRatingByProductId(productId, status);
    }

    @Override
    public Long countByProductIdAndStatus(Long productId, ReviewStatus status) {
        return repository.countByProductIdAndStatus(productId, status);
    }

    @Override
    public int autoApproveOlderThan(LocalDateTime threshold) {
        return repository.bulkApproveOlderThan(
                ReviewStatus.APROBADO, ReviewStatus.PENDIENTE, threshold, LocalDateTime.now());
    }

    @Override
    public ProductReview save(ProductReview review) {
        return repository.save(review);
    }

    @Override
    public void delete(ProductReview review) {
        repository.delete(review);
    }
}
