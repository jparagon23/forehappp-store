package com.forehapp.store.ambassadorModule.domain.ports.in;

import com.forehapp.store.ambassadorModule.application.dto.AmbassadorStatsDto;
import com.forehapp.store.ambassadorModule.application.dto.AmbassadorValidationResponse;
import com.forehapp.store.ambassadorModule.application.dto.CommissionResponse;

import java.util.List;

public interface IAmbassadorDashboardService {
    AmbassadorStatsDto getMyStats(Long userId);
    List<CommissionResponse> getMyCommissions(Long userId);
    AmbassadorValidationResponse validateCode(String referralCode);
}
