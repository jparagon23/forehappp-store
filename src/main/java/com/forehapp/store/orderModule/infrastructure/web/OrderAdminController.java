package com.forehapp.store.orderModule.infrastructure.web;

import com.forehapp.store.orderModule.domain.ports.in.IOrderModuleService;
import com.forehapp.store.orderModule.infrastructure.web.dto.CancelGroupRequestDto;
import com.forehapp.store.orderModule.infrastructure.web.dto.SellerOrderGroupDto;
import com.forehapp.store.orderModule.infrastructure.web.dto.ShipGroupRequestDto;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/seller/order-groups")
public class OrderAdminController {

    private final IOrderModuleService orderModuleService;

    public OrderAdminController(IOrderModuleService orderModuleService) {
        this.orderModuleService = orderModuleService;
    }

    @GetMapping
    public ResponseEntity<List<SellerOrderGroupDto>> getMyGroups(
            @AuthenticationPrincipal String userId) {
        return ResponseEntity.ok(orderModuleService.getSellerGroups(Long.parseLong(userId)));
    }

    @GetMapping("/{groupId}")
    public ResponseEntity<SellerOrderGroupDto> getGroupById(
            @AuthenticationPrincipal String userId,
            @PathVariable Long groupId) {
        return ResponseEntity.ok(orderModuleService.getSellerGroupById(Long.parseLong(userId), groupId));
    }

    @PatchMapping("/{groupId}/prepare")
    public ResponseEntity<Void> prepare(
            @AuthenticationPrincipal String userId,
            @PathVariable Long groupId) {
        orderModuleService.prepareGroup(Long.parseLong(userId), groupId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{groupId}/ship")
    public ResponseEntity<Void> ship(
            @AuthenticationPrincipal String userId,
            @PathVariable Long groupId,
            @Valid @RequestBody ShipGroupRequestDto dto) {
        orderModuleService.shipGroup(Long.parseLong(userId), groupId, dto.trackingNumber());
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{groupId}/deliver")
    public ResponseEntity<Void> deliver(
            @AuthenticationPrincipal String userId,
            @PathVariable Long groupId) {
        orderModuleService.deliverGroup(Long.parseLong(userId), groupId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{groupId}/cancel")
    public ResponseEntity<Void> cancel(
            @AuthenticationPrincipal String userId,
            @PathVariable Long groupId,
            @Valid @RequestBody CancelGroupRequestDto dto) {
        orderModuleService.cancelGroup(Long.parseLong(userId), groupId, dto.reason());
        return ResponseEntity.noContent().build();
    }
}
