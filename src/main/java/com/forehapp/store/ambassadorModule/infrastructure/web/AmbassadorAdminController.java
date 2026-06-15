package com.forehapp.store.ambassadorModule.infrastructure.web;

import com.forehapp.store.ambassadorModule.application.dto.*;
import com.forehapp.store.ambassadorModule.domain.ports.in.IAmbassadorAdminService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/ambassadors")
public class AmbassadorAdminController {

    private final IAmbassadorAdminService adminService;

    public AmbassadorAdminController(IAmbassadorAdminService adminService) {
        this.adminService = adminService;
    }

    @PostMapping
    public ResponseEntity<AmbassadorResponse> create(
            @Valid @RequestBody CreateAmbassadorRequestDto dto,
            @AuthenticationPrincipal String userId) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(adminService.create(Long.parseLong(userId), dto));
    }

    @GetMapping
    public ResponseEntity<List<AmbassadorResponse>> findAll(@AuthenticationPrincipal String userId) {
        return ResponseEntity.ok(adminService.findAll(Long.parseLong(userId)));
    }

    @GetMapping("/{ambassadorId}")
    public ResponseEntity<AmbassadorResponse> findById(
            @PathVariable Long ambassadorId,
            @AuthenticationPrincipal String userId) {
        return ResponseEntity.ok(adminService.findById(Long.parseLong(userId), ambassadorId));
    }

    @PutMapping("/{ambassadorId}")
    public ResponseEntity<AmbassadorResponse> update(
            @PathVariable Long ambassadorId,
            @Valid @RequestBody UpdateAmbassadorRequestDto dto,
            @AuthenticationPrincipal String userId) {
        return ResponseEntity.ok(adminService.update(Long.parseLong(userId), ambassadorId, dto));
    }

    @GetMapping("/{ambassadorId}/commissions")
    public ResponseEntity<List<CommissionResponse>> getCommissions(
            @PathVariable Long ambassadorId,
            @AuthenticationPrincipal String userId) {
        return ResponseEntity.ok(adminService.findCommissionsByAmbassador(Long.parseLong(userId), ambassadorId));
    }

    @PutMapping("/commissions/{commissionId}/pay")
    public ResponseEntity<CommissionResponse> payCommission(
            @PathVariable Long commissionId,
            @AuthenticationPrincipal String userId) {
        return ResponseEntity.ok(adminService.payCommission(Long.parseLong(userId), commissionId));
    }
}
