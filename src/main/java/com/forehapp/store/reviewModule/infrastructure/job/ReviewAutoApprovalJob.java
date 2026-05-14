package com.forehapp.store.reviewModule.infrastructure.job;

import com.forehapp.store.reviewModule.domain.ports.out.IReviewDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class ReviewAutoApprovalJob {

    private static final Logger log = LoggerFactory.getLogger(ReviewAutoApprovalJob.class);

    private final IReviewDao reviewDao;

    public ReviewAutoApprovalJob(IReviewDao reviewDao) {
        this.reviewDao = reviewDao;
    }

    @Scheduled(cron = "0 0 * * * *")
    public void autoApprovePendingReviews() {
        LocalDateTime threshold = LocalDateTime.now().minusDays(1);
        int count = reviewDao.autoApproveOlderThan(threshold);
        if (count > 0) {
            log.info("Auto-approved {} review(s) pending for more than 1 day", count);
        }
    }
}
