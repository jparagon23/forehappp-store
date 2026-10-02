package com.forehapp.store.repurchaseModule.infrastructure.web;

import com.forehapp.store.repurchaseModule.application.dto.ReminderRunSummary;
import com.forehapp.store.repurchaseModule.domain.ports.in.IRepurchaseReminderService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/repurchase-reminders")
public class AdminRepurchaseReminderController {

    private final IRepurchaseReminderService reminderService;

    public AdminRepurchaseReminderController(IRepurchaseReminderService reminderService) {
        this.reminderService = reminderService;
    }

    // dryRun defaults to true so calling this by mistake only previews who would be emailed
    @PostMapping("/run")
    public ResponseEntity<ReminderRunSummary> run(@RequestParam(defaultValue = "true") boolean dryRun,
                                                  @AuthenticationPrincipal String userId) {
        return ResponseEntity.ok(reminderService.runAsAdmin(Long.parseLong(userId), dryRun));
    }
}
