package com.forehapp.store.userModule.infrastructure.web;

import com.forehapp.store.userModule.application.dto.UserStatsResponse;
import com.forehapp.store.userModule.domain.ports.in.IAdminUserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/users")
public class AdminUserController {

    private final IAdminUserService adminUserService;

    public AdminUserController(IAdminUserService adminUserService) {
        this.adminUserService = adminUserService;
    }

    @GetMapping("/stats")
    public ResponseEntity<UserStatsResponse> getUserStats(@AuthenticationPrincipal String userId) {
        return ResponseEntity.ok(adminUserService.getUserStats(Long.parseLong(userId)));
    }
}
