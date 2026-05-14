package com.forehapp.store.returnModule.infrastructure.web;

import com.forehapp.store.returnModule.application.dto.ApproveReturnRequestDto;
import com.forehapp.store.returnModule.application.dto.RejectReturnRequestDto;
import com.forehapp.store.returnModule.application.dto.ReturnResponse;
import com.forehapp.store.returnModule.domain.ports.in.IReturnModuleService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/returns")
public class ReturnAdminController {

    private final IReturnModuleService returnModuleService;

    public ReturnAdminController(IReturnModuleService returnModuleService) {
        this.returnModuleService = returnModuleService;
    }

    @GetMapping("/pending")
    public Page<ReturnResponse> getPendingReturns(
            @AuthenticationPrincipal String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return returnModuleService.getPendingReturns(Long.parseLong(userId), page, size);
    }

    @PatchMapping("/{returnId}/approve")
    public ReturnResponse approveReturn(
            @AuthenticationPrincipal String userId,
            @PathVariable Long returnId,
            @Valid @RequestBody ApproveReturnRequestDto dto) {
        return returnModuleService.approveReturn(Long.parseLong(userId), returnId, dto);
    }

    @PatchMapping("/{returnId}/reject")
    public ReturnResponse rejectReturn(
            @AuthenticationPrincipal String userId,
            @PathVariable Long returnId,
            @Valid @RequestBody RejectReturnRequestDto dto) {
        return returnModuleService.rejectReturn(Long.parseLong(userId), returnId, dto);
    }

    @PatchMapping("/{returnId}/refunded")
    public ReturnResponse markRefunded(
            @AuthenticationPrincipal String userId,
            @PathVariable Long returnId) {
        return returnModuleService.markRefunded(Long.parseLong(userId), returnId);
    }
}
