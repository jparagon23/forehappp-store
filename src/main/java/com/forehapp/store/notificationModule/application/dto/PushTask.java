package com.forehapp.store.notificationModule.application.dto;

import com.forehapp.store.notificationModule.domain.model.NotificationType;

public record PushTask(Long userId, String title, String message, String actionUrl, NotificationType type) {}
