package com.forehapp.store.repurchaseModule.infrastructure.job;

import com.forehapp.store.repurchaseModule.application.RepurchaseSettings;
import com.forehapp.store.repurchaseModule.application.dto.ReminderRunSummary;
import com.forehapp.store.repurchaseModule.application.usecases.RepurchaseReminderServiceImpl;
import com.forehapp.store.repurchaseModule.domain.ports.in.IRepurchaseReminderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class RepurchaseReminderJob {

    private static final Logger log = LoggerFactory.getLogger(RepurchaseReminderJob.class);

    private final IRepurchaseReminderService reminderService;
    private final RepurchaseSettings settings;

    public RepurchaseReminderJob(IRepurchaseReminderService reminderService, RepurchaseSettings settings) {
        this.reminderService = reminderService;
        this.settings = settings;
    }

    // Pinned to the store's timezone: this one lands in a buyer's inbox, so the hour matters
    @Scheduled(cron = "${app.repurchase.cron:0 0 10 * * *}", zone = "America/Bogota")
    public void sendRepurchaseReminders() {
        if (!settings.isEnabled()) {
            return;
        }
        try {
            ReminderRunSummary summary = reminderService.run(
                    LocalDate.now(RepurchaseReminderServiceImpl.STORE_ZONE), false);
            if (summary.emails() > 0) {
                log.info("Sent {} repurchase reminder email(s) covering {} product(s)",
                        summary.emails(), summary.products());
            }
        } catch (Exception e) {
            log.error("Repurchase reminder job failed", e);
        }
    }
}
