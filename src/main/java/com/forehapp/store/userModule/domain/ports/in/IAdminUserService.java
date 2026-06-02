package com.forehapp.store.userModule.domain.ports.in;

import com.forehapp.store.userModule.application.dto.UserStatsResponse;

public interface IAdminUserService {
    UserStatsResponse getUserStats(Long adminUserId);
}
