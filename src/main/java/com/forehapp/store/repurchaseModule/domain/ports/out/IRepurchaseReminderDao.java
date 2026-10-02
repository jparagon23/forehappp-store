package com.forehapp.store.repurchaseModule.domain.ports.out;

import com.forehapp.store.repurchaseModule.domain.model.PurchaseRow;
import com.forehapp.store.repurchaseModule.domain.model.RepurchaseReminder;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface IRepurchaseReminderDao {
    List<PurchaseRow> findPurchasesSince(LocalDateTime since);
    Set<Long> findRemindedOrderItemIds(Collection<Long> orderItemIds);
    List<RepurchaseReminder> findByEmails(Collection<String> emails);
    Map<String, LocalDateTime> findLastOrderAtByEmails(Collection<String> emails);
    Map<Long, String> findThumbnailUrls(Collection<Long> productIds);
    void saveAll(List<RepurchaseReminder> reminders);
}
