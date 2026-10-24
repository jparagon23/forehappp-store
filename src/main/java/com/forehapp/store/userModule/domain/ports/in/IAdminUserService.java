package com.forehapp.store.userModule.domain.ports.in;

import com.forehapp.store.userModule.application.dto.UserStatsResponse;
import com.forehapp.store.userModule.domain.model.UserSearchResult;

import java.util.List;

public interface IAdminUserService {
    UserStatsResponse getUserStats(Long adminUserId);

    List<UserSearchResult> searchUsers(Long adminUserId, String query);
}
