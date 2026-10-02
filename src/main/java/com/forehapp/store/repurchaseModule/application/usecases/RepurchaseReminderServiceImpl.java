package com.forehapp.store.repurchaseModule.application.usecases;

import com.forehapp.store.general.exceptions.ErrorCode;
import com.forehapp.store.general.exceptions.ForbiddenException;
import com.forehapp.store.general.exceptions.NotFoundException;
import com.forehapp.store.notificationModule.domain.ports.in.NotificationUseCase;
import com.forehapp.store.repurchaseModule.application.RepurchaseSettings;
import com.forehapp.store.repurchaseModule.application.dto.ReminderRunSummary;
import com.forehapp.store.repurchaseModule.domain.model.PurchaseRow;
import com.forehapp.store.repurchaseModule.domain.model.ReminderPlan;
import com.forehapp.store.repurchaseModule.domain.model.RepurchaseReminder;
import com.forehapp.store.repurchaseModule.domain.ports.in.IRepurchaseReminderService;
import com.forehapp.store.repurchaseModule.domain.ports.out.IEmailUnsubscribeDao;
import com.forehapp.store.repurchaseModule.domain.ports.out.IRepurchaseReminderDao;
import com.forehapp.store.userModule.domain.model.StoreProfile;
import com.forehapp.store.userModule.domain.model.StoreRole;
import com.forehapp.store.userModule.domain.ports.out.IStoreProfileDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class RepurchaseReminderServiceImpl implements IRepurchaseReminderService {

    public static final ZoneId STORE_ZONE = ZoneId.of("America/Bogota");

    private static final Logger log = LoggerFactory.getLogger(RepurchaseReminderServiceImpl.class);
    // Slack on top of the max duration so orders delivered late are still inside the scan
    private static final int LOOKBACK_MARGIN_DAYS = 30;

    private final IRepurchaseReminderDao reminderDao;
    private final IEmailUnsubscribeDao unsubscribeDao;
    private final IStoreProfileDao storeProfileDao;
    private final ReminderSelector selector;
    private final RepurchaseEmailBuilder emailBuilder;
    private final NotificationUseCase notificationUseCase;
    private final RepurchaseSettings settings;

    public RepurchaseReminderServiceImpl(IRepurchaseReminderDao reminderDao,
                                         IEmailUnsubscribeDao unsubscribeDao,
                                         IStoreProfileDao storeProfileDao,
                                         ReminderSelector selector,
                                         RepurchaseEmailBuilder emailBuilder,
                                         NotificationUseCase notificationUseCase,
                                         RepurchaseSettings settings) {
        this.reminderDao = reminderDao;
        this.unsubscribeDao = unsubscribeDao;
        this.storeProfileDao = storeProfileDao;
        this.selector = selector;
        this.emailBuilder = emailBuilder;
        this.notificationUseCase = notificationUseCase;
        this.settings = settings;
    }

    @Override
    public ReminderRunSummary runAsAdmin(Long adminUserId, boolean dryRun) {
        StoreProfile profile = storeProfileDao.findByUserId(adminUserId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.USER_PROFILE_NOT_FOUND, "Store profile not found"));
        if (!profile.getRoles().contains(StoreRole.STORE_ADMIN)) {
            throw new ForbiddenException(ErrorCode.STORE_ADMIN_REQUIRED, "Admin access required");
        }
        return run(LocalDate.now(STORE_ZONE), dryRun);
    }

    @Override
    public ReminderRunSummary run(LocalDate today, boolean dryRun) {
        List<ReminderPlan> plans = selectPlans(today);

        List<ReminderPlan> processed = new ArrayList<>();
        if (dryRun) {
            processed.addAll(plans);
        } else {
            Set<Long> productIds = plans.stream()
                    .flatMap(p -> p.items().stream())
                    .map(ReminderPlan.Item::productId)
                    .collect(Collectors.toSet());
            Map<Long, String> thumbnails = reminderDao.findThumbnailUrls(productIds);

            LocalDateTime sentAt = LocalDateTime.now();
            for (ReminderPlan plan : plans) {
                if (send(plan, thumbnails, today, sentAt)) {
                    processed.add(plan);
                }
            }
        }
        return toSummary(processed, dryRun);
    }

    private List<ReminderPlan> selectPlans(LocalDate today) {
        int lookbackDays = settings.getMaxDurationDays() + settings.getLateWindowDays() + LOOKBACK_MARGIN_DAYS;
        List<PurchaseRow> rows = reminderDao.findPurchasesSince(today.minusDays(lookbackDays).atStartOfDay());
        if (rows.isEmpty()) return List.of();

        Set<Long> itemIds = rows.stream().map(PurchaseRow::itemId).collect(Collectors.toSet());
        List<ReminderPlan> due = selector.findDuePlans(rows, reminderDao.findRemindedOrderItemIds(itemIds), today);
        if (due.isEmpty()) return List.of();

        Set<String> emails = due.stream().map(ReminderPlan::email).collect(Collectors.toSet());
        Map<String, Set<LocalDateTime>> sentAtByEmail = reminderDao.findByEmails(emails).stream()
                .collect(Collectors.groupingBy(
                        r -> ReminderSelector.normalizeEmail(r.getEmail()),
                        Collectors.mapping(RepurchaseReminder::getSentAt, Collectors.toSet())));

        return selector.applyRecipientRules(
                due,
                unsubscribeDao.findUnsubscribed(emails),
                sentAtByEmail,
                reminderDao.findLastOrderAtByEmails(emails),
                today);
    }

    private boolean send(ReminderPlan plan, Map<Long, String> thumbnails, LocalDate today, LocalDateTime sentAt) {
        List<RepurchaseReminder> reminders = new ArrayList<>();
        for (ReminderPlan.Item item : plan.items()) {
            for (Long orderItemId : item.orderItemIds()) {
                RepurchaseReminder reminder = new RepurchaseReminder();
                reminder.setOrderItemId(orderItemId);
                reminder.setOrderId(item.orderId());
                reminder.setProductId(item.productId());
                reminder.setEmail(plan.email());
                reminder.setDueDate(item.dueDate());
                reminder.setSentAt(sentAt);
                reminders.add(reminder);
            }
        }

        // Record first, send after: the unique key on order_item_id makes a concurrent run fail here
        // instead of emailing the buyer twice.
        try {
            reminderDao.saveAll(reminders);
        } catch (DataIntegrityViolationException e) {
            log.warn("[RepurchaseReminder] Skipping email={}: reminder already recorded by another run", plan.email());
            return false;
        }

        notificationUseCase.sendEmailNotification(
                plan.email(),
                emailBuilder.buildSubject(plan),
                emailBuilder.buildHtml(plan, thumbnails, today));
        return true;
    }

    private ReminderRunSummary toSummary(List<ReminderPlan> plans, boolean dryRun) {
        List<ReminderRunSummary.Recipient> recipients = plans.stream()
                .map(plan -> new ReminderRunSummary.Recipient(
                        plan.email(),
                        plan.items().stream()
                                .map(i -> new ReminderRunSummary.Product(
                                        i.productId(), i.productTitle(), i.orderId(), i.purchasedOn(), i.dueDate()))
                                .toList()))
                .toList();
        int products = plans.stream().mapToInt(p -> p.items().size()).sum();
        return new ReminderRunSummary(dryRun, plans.size(), products, recipients);
    }
}
