package com.forehapp.store.reviewModule.application.usecases;

import com.forehapp.store.productModule.domain.model.Product;
import com.forehapp.store.productModule.domain.ports.out.IProductDao;
import com.forehapp.store.reviewModule.application.dto.CreateReviewRequestDto;
import com.forehapp.store.reviewModule.application.dto.ProductRatingSummary;
import com.forehapp.store.reviewModule.application.dto.ReviewPageResponse;
import com.forehapp.store.reviewModule.application.dto.ReviewResponse;
import com.forehapp.store.reviewModule.domain.model.ProductReview;
import com.forehapp.store.reviewModule.domain.model.ReviewStatus;
import com.forehapp.store.reviewModule.domain.ports.in.IReviewService;
import com.forehapp.store.reviewModule.domain.ports.out.IReviewDao;
import com.forehapp.store.userModule.domain.model.StoreProfile;
import com.forehapp.store.userModule.domain.ports.out.IStoreProfileDao;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class ReviewServiceImpl implements IReviewService {

    private final IReviewDao reviewDao;
    private final IProductDao productDao;
    private final IStoreProfileDao storeProfileDao;

    public ReviewServiceImpl(IReviewDao reviewDao, IProductDao productDao, IStoreProfileDao storeProfileDao) {
        this.reviewDao = reviewDao;
        this.productDao = productDao;
        this.storeProfileDao = storeProfileDao;
    }

    @Override
    @Transactional(readOnly = true)
    public ReviewPageResponse getProductReviews(Long productId, int page, int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<ProductReview> reviewPage = reviewDao.findByProductIdAndStatus(productId, ReviewStatus.APROBADO, pageable);

        Double avg = reviewDao.findAverageRatingByProductId(productId, ReviewStatus.APROBADO);

        ProductRatingSummary summary = new ProductRatingSummary(productId, avg != null ? avg : 0.0, reviewPage.getTotalElements());
        List<ReviewResponse> reviews = reviewPage.getContent().stream().map(this::toResponse).toList();

        return new ReviewPageResponse(summary, reviews, reviewPage.getNumber(), reviewPage.getTotalPages(), reviewPage.getTotalElements());
    }

    @Override
    @Transactional(readOnly = true)
    public ProductRatingSummary getProductRatingSummary(Long productId) {
        Double avg = reviewDao.findAverageRatingByProductId(productId, ReviewStatus.APROBADO);
        Long total = reviewDao.countByProductIdAndStatus(productId, ReviewStatus.APROBADO);
        return new ProductRatingSummary(productId, avg != null ? avg : 0.0, total != null ? total : 0L);
    }

    @Override
    @Transactional
    public ReviewResponse createReview(Long userId, Long productId, CreateReviewRequestDto dto) {
        StoreProfile reviewer = resolveProfile(userId);
        Product product = productDao.findById(productId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));

        reviewDao.findByProductIdAndReviewerId(productId, reviewer.getId()).ifPresent(r -> {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "You have already reviewed this product");
        });

        ProductReview review = new ProductReview();
        review.setProduct(product);
        review.setReviewer(reviewer);
        review.setRating(dto.rating());
        review.setTitle(dto.title());
        review.setComment(dto.comment());

        return toResponse(reviewDao.save(review));
    }

    @Override
    @Transactional
    public void deleteReview(Long userId, Long reviewId) {
        StoreProfile reviewer = resolveProfile(userId);
        ProductReview review = reviewDao.findById(reviewId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Review not found"));

        if (!review.getReviewer().getId().equals(reviewer.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not your review");
        }
        if (review.getStatus() == ReviewStatus.APROBADO) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot delete an approved review");
        }

        reviewDao.delete(review);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReviewResponse> getMyReviews(Long userId) {
        StoreProfile reviewer = resolveProfile(userId);
        return reviewDao.findByReviewerId(reviewer.getId()).stream().map(this::toResponse).toList();
    }

    private StoreProfile resolveProfile(Long userId) {
        return storeProfileDao.findByUserId(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Store profile not found"));
    }

    private ReviewResponse toResponse(ProductReview r) {
        String reviewerName = r.getReviewer().getUser().getName() + " " + r.getReviewer().getUser().getLastname();
        return new ReviewResponse(
                r.getId(),
                r.getProduct().getId(),
                r.getProduct().getTitle(),
                r.getReviewer().getId(),
                reviewerName.trim(),
                r.getRating(),
                r.getTitle(),
                r.getComment(),
                r.getStatus().name(),
                r.getCreatedAt()
        );
    }
}
