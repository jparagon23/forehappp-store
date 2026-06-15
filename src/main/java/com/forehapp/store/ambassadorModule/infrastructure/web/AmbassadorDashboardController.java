package com.forehapp.store.ambassadorModule.infrastructure.web;

import com.forehapp.store.ambassadorModule.application.dto.AmbassadorStatsDto;
import com.forehapp.store.ambassadorModule.application.dto.CommissionResponse;
import com.forehapp.store.ambassadorModule.domain.ports.in.IAmbassadorDashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/ambassadors")
public class AmbassadorDashboardController {

    private final IAmbassadorDashboardService dashboardService;

    public AmbassadorDashboardController(IAmbassadorDashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/me")
    public ResponseEntity<AmbassadorStatsDto> getMyStats(@AuthenticationPrincipal String userId) {
        return ResponseEntity.ok(dashboardService.getMyStats(Long.parseLong(userId)));
    }

    @GetMapping("/me/commissions")
    public ResponseEntity<List<CommissionResponse>> getMyCommissions(@AuthenticationPrincipal String userId) {
        return ResponseEntity.ok(dashboardService.getMyCommissions(Long.parseLong(userId)));
    }
}
