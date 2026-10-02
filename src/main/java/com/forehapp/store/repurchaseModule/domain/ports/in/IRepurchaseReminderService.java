package com.forehapp.store.repurchaseModule.domain.ports.in;

import com.forehapp.store.repurchaseModule.application.dto.ReminderRunSummary;

import java.time.LocalDate;

public interface IRepurchaseReminderService {
    ReminderRunSummary run(LocalDate today, boolean dryRun);
    ReminderRunSummary runAsAdmin(Long adminUserId, boolean dryRun);
}
