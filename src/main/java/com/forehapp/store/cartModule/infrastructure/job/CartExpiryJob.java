package com.forehapp.store.cartModule.infrastructure.job;

import com.forehapp.store.cartModule.domain.ports.out.ICartDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class CartExpiryJob {

    private static final Logger log = LoggerFactory.getLogger(CartExpiryJob.class);
    private static final int EXPIRY_DAYS = 180;

    private final ICartDao cartDao;

    public CartExpiryJob(ICartDao cartDao) {
        this.cartDao = cartDao;
    }

    // NUEVA-BUG-04: bulk expire carts daily so GET /cart rarely needs to write
    @Scheduled(cron = "0 0 2 * * *")
    public void expireAbandonedCarts() {
        LocalDateTime threshold = LocalDateTime.now().minusDays(EXPIRY_DAYS);
        int count = cartDao.expireOldCarts(threshold);
        if (count > 0) {
            log.info("Expired {} abandoned cart(s) inactive for more than {} days", count, EXPIRY_DAYS);
        }
    }
}
