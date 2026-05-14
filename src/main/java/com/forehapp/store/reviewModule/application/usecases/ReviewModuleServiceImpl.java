package com.forehapp.store.reviewModule.application.usecases;

import com.forehapp.store.reviewModule.application.dto.ReviewPageResponse;
import com.forehapp.store.reviewModule.application.dto.ReviewResponse;
import com.forehapp.store.reviewModule.domain.model.ProductReview;
import com.forehapp.store.reviewModule.domain.model.ReviewStatus;
import com.forehapp.store.reviewModule.domain.ports.in.IReviewModuleService;
import com.forehapp.store.reviewModule.domain.ports.out.IReviewDao;
import com.forehapp.store.userModule.domain.model.StoreProfile;
import com.forehapp.store.userModule.domain.model.StoreRole;
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
public class ReviewModuleServiceImpl implements IReviewModuleService {

    private final IReviewDao reviewDao;
    private final IStoreProfileDao storeProfileDao;

    public ReviewModuleServiceImpl(IReviewDao reviewDao, IStoreProfileDao storeProfileDao) {
        this.reviewDao = reviewDao;
        this.storeProfileDao = storeProfileDao;
    }

    @Override
    @Transactional(readOnly = true)
    public ReviewPageResponse getPendingReviews(Long userId, int page, int size) {
        requireAdmin(userId);
        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "createdAt"));
        Page<ProductReview> reviewPage = reviewDao.findByStatus(ReviewStatus.PENDIENTE, pageable);

        List<ReviewResponse> reviews = reviewPage.getContent().stream().map(this::toResponse).toList();
        return new ReviewPageResponse(null, reviews, reviewPage.getNumber(), reviewPage.getTotalPages(), reviewPage.getTotalElements());
    }

    @Override
    @Transactional
    public ReviewResponse approveReview(Long userId, Long reviewId) {
        requireAdmin(userId);
        ProductReview review = findPendingReview(reviewId);
        review.setStatus(ReviewStatus.APROBADO);
        return toResponse(reviewDao.save(review));
    }

    @Override
    @Transactional
    public ReviewResponse rejectReview(Long userId, Long reviewId) {
        requireAdmin(userId);
        ProductReview review = findPendingReview(reviewId);
        review.setStatus(ReviewStatus.RECHAZADO);
        return toResponse(reviewDao.save(review));
    }

    private ProductReview findPendingReview(Long reviewId) {
        ProductReview review = reviewDao.findById(reviewId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Review not found"));
        if (review.getStatus() != ReviewStatus.PENDIENTE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Review is not pending");
        }
        return review;
    }

    private void requireAdmin(Long userId) {
        StoreProfile profile = storeProfileDao.findByUserId(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Store profile not found"));
        if (!profile.getRoles().contains(StoreRole.STORE_ADMIN)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Admin access required");
        }
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
