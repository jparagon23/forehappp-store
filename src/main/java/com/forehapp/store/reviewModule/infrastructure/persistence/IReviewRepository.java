package com.forehapp.store.reviewModule.infrastructure.persistence;

import com.forehapp.store.reviewModule.domain.model.ProductReview;
import com.forehapp.store.reviewModule.domain.model.ReviewStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface IReviewRepository extends JpaRepository<ProductReview, Long> {

    @Query("SELECT r FROM ProductReview r JOIN FETCH r.reviewer rv JOIN FETCH rv.user JOIN FETCH r.product WHERE r.id = :id")
    Optional<ProductReview> findByIdWithDetails(@Param("id") Long id);

    @Query("SELECT r FROM ProductReview r WHERE r.product.id = :productId AND r.reviewer.id = :reviewerId")
    Optional<ProductReview> findByProductIdAndReviewerId(@Param("productId") Long productId, @Param("reviewerId") Long reviewerId);

    @Query(value = "SELECT r FROM ProductReview r JOIN FETCH r.reviewer rv JOIN FETCH rv.user JOIN FETCH r.product WHERE r.product.id = :productId AND r.status = :status",
           countQuery = "SELECT COUNT(r) FROM ProductReview r WHERE r.product.id = :productId AND r.status = :status")
    Page<ProductReview> findByProductIdAndStatus(@Param("productId") Long productId, @Param("status") ReviewStatus status, Pageable pageable);

    @Query("SELECT r FROM ProductReview r JOIN FETCH r.reviewer rv JOIN FETCH rv.user JOIN FETCH r.product WHERE r.reviewer.id = :reviewerId")
    List<ProductReview> findByReviewerId(@Param("reviewerId") Long reviewerId);

    @Query(value = "SELECT r FROM ProductReview r JOIN FETCH r.reviewer rv JOIN FETCH rv.user JOIN FETCH r.product WHERE r.status = :status",
           countQuery = "SELECT COUNT(r) FROM ProductReview r WHERE r.status = :status")
    Page<ProductReview> findByStatus(@Param("status") ReviewStatus status, Pageable pageable);

    @Query("SELECT AVG(r.rating) FROM ProductReview r WHERE r.product.id = :productId AND r.status = :status")
    Double findAverageRatingByProductId(@Param("productId") Long productId, @Param("status") ReviewStatus status);

    @Query("SELECT COUNT(r) FROM ProductReview r WHERE r.product.id = :productId AND r.status = :status")
    Long countByProductIdAndStatus(@Param("productId") Long productId, @Param("status") ReviewStatus status);
}
