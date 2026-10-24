package com.forehapp.store.userModule.infrastructure.web;

import com.forehapp.store.userModule.application.dto.UserStatsResponse;
import com.forehapp.store.userModule.domain.model.UserSearchResult;
import com.forehapp.store.userModule.domain.ports.in.IAdminUserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

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

    @GetMapping("/search")
    public ResponseEntity<List<UserSearchResult>> searchUsers(@AuthenticationPrincipal String userId,
                                                              @RequestParam(name = "q", defaultValue = "") String query) {
        return ResponseEntity.ok(adminUserService.searchUsers(Long.parseLong(userId), query));
    }
}
