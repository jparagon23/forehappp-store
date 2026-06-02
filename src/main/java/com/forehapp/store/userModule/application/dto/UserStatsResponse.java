package com.forehapp.store.userModule.application.dto;

import com.forehapp.store.userModule.domain.model.RegistrationTrendPoint;
import com.forehapp.store.userModule.domain.model.UserRegistrationSummary;

import java.util.List;

public record UserStatsResponse(
        long totalUsers,
        long newUsersToday,
        long newUsersThisWeek,
        long newUsersThisMonth,
        long usersWhoOrdered,
        double conversionRate,
        long usersWithPhone,
        long activeUsers,
        List<UserRegistrationSummary> recentUsers,
        List<RegistrationTrendPoint> registrationTrend
) {}
