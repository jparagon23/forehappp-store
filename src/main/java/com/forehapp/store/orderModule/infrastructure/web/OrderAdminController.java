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
@RequestMapping("/api/v1/stores/{storeId}/order-groups")
public class OrderAdminController {

    private final IOrderModuleService orderModuleService;

    public OrderAdminController(IOrderModuleService orderModuleService) {
        this.orderModuleService = orderModuleService;
    }

    @GetMapping
    public ResponseEntity<List<SellerOrderGroupDto>> getMyGroups(
            @PathVariable Long storeId,
            @AuthenticationPrincipal String userId) {
        return ResponseEntity.ok(orderModuleService.getSellerGroups(storeId, Long.parseLong(userId)));
    }

    @GetMapping("/{groupId}")
    public ResponseEntity<SellerOrderGroupDto> getGroupById(
            @PathVariable Long storeId,
            @PathVariable Long groupId,
            @AuthenticationPrincipal String userId) {
        return ResponseEntity.ok(orderModuleService.getSellerGroupById(storeId, groupId, Long.parseLong(userId)));
    }

    @PatchMapping("/{groupId}/prepare")
    public ResponseEntity<Void> prepare(
            @PathVariable Long storeId,
            @PathVariable Long groupId,
            @AuthenticationPrincipal String userId) {
        orderModuleService.prepareGroup(storeId, groupId, Long.parseLong(userId));
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{groupId}/ship")
    public ResponseEntity<Void> ship(
            @PathVariable Long storeId,
            @PathVariable Long groupId,
            @Valid @RequestBody ShipGroupRequestDto dto,
            @AuthenticationPrincipal String userId) {
        orderModuleService.shipGroup(storeId, groupId, dto.trackingNumber(), Long.parseLong(userId));
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{groupId}/deliver")
    public ResponseEntity<Void> deliver(
            @PathVariable Long storeId,
            @PathVariable Long groupId,
            @AuthenticationPrincipal String userId) {
        orderModuleService.deliverGroup(storeId, groupId, Long.parseLong(userId));
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{groupId}/cancel")
    public ResponseEntity<Void> cancel(
            @PathVariable Long storeId,
            @PathVariable Long groupId,
            @Valid @RequestBody CancelGroupRequestDto dto,
            @AuthenticationPrincipal String userId) {
        orderModuleService.cancelGroup(storeId, groupId, dto.reason(), Long.parseLong(userId));
        return ResponseEntity.noContent().build();
    }
}
