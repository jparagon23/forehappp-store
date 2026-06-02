package com.forehapp.store.userModule.application.dto;

import com.forehapp.store.userModule.domain.model.RegistrationTrendPoint;
import com.forehapp.store.userModule.domain.model.UserRegistrationSummary;

import java.util.List;

public record UserStatsResponse(
        long totalUsers,
        List<UserRegistrationSummary> recentUsers,
        List<RegistrationTrendPoint> registrationTrend
) {}
