package com.forehapp.store.repurchaseModule.infrastructure.persistence;

import com.forehapp.store.repurchaseModule.domain.model.PurchaseRow;
import com.forehapp.store.repurchaseModule.domain.model.RepurchaseReminder;
import com.forehapp.store.repurchaseModule.domain.ports.out.IRepurchaseReminderDao;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Repository
public class RepurchaseReminderRepositoryImpl implements IRepurchaseReminderDao {

    private final IRepurchaseReminderJpaRepository jpaRepository;

    public RepurchaseReminderRepositoryImpl(IRepurchaseReminderJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public List<PurchaseRow> findPurchasesSince(LocalDateTime since) {
        return jpaRepository.findPurchasesSince(since);
    }

    @Override
    public Set<Long> findRemindedOrderItemIds(Collection<Long> orderItemIds) {
        if (orderItemIds.isEmpty()) return Set.of();
        return new HashSet<>(jpaRepository.findRemindedOrderItemIds(orderItemIds));
    }

    @Override
    public List<RepurchaseReminder> findByEmails(Collection<String> emails) {
        if (emails.isEmpty()) return List.of();
        return jpaRepository.findByEmailIn(emails);
    }

    @Override
    public Map<String, LocalDateTime> findLastOrderAtByEmails(Collection<String> emails) {
        if (emails.isEmpty()) return Map.of();
        Map<String, LocalDateTime> result = new HashMap<>();
        for (Object[] row : jpaRepository.findLastOrderAtByEmails(emails)) {
            result.put((String) row[0], (LocalDateTime) row[1]);
        }
        return result;
    }

    @Override
    public Map<Long, String> findThumbnailUrls(Collection<Long> productIds) {
        if (productIds.isEmpty()) return Map.of();
        Map<Long, String> result = new HashMap<>();
        for (Object[] row : jpaRepository.findImageUrlsByProductIds(productIds)) {
            result.putIfAbsent((Long) row[0], (String) row[1]);
        }
        return result;
    }

    @Override
    public void saveAll(List<RepurchaseReminder> reminders) {
        jpaRepository.saveAllAndFlush(reminders);
    }
}
