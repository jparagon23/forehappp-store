package com.forehapp.store.repurchaseModule.application;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Getter
@Component
public class RepurchaseSettings {

    private final boolean enabled;
    private final double sendRatio;
    private final int lateWindowDays;
    private final int minDaysBetweenEmails;
    private final int maxIgnoredReminders;
    private final int maxDurationDays;
    private final int shippedFallbackDays;

    public RepurchaseSettings(
            @Value("${app.repurchase.enabled:false}") boolean enabled,
            @Value("${app.repurchase.send-ratio:0.8}") double sendRatio,
            @Value("${app.repurchase.late-window-days:7}") int lateWindowDays,
            @Value("${app.repurchase.min-days-between-emails:30}") int minDaysBetweenEmails,
            @Value("${app.repurchase.max-ignored-reminders:2}") int maxIgnoredReminders,
            @Value("${app.repurchase.max-duration-days:365}") int maxDurationDays,
            @Value("${app.repurchase.shipped-fallback-days:3}") int shippedFallbackDays) {
        this.enabled = enabled;
        this.sendRatio = sendRatio;
        this.lateWindowDays = lateWindowDays;
        this.minDaysBetweenEmails = minDaysBetweenEmails;
        this.maxIgnoredReminders = maxIgnoredReminders;
        this.maxDurationDays = maxDurationDays;
        this.shippedFallbackDays = shippedFallbackDays;
    }
}
