package com.forehapp.store.orderModule.infrastructure.web;

import com.forehapp.store.orderModule.domain.ports.in.IOrderItemsEditService;
import com.forehapp.store.orderModule.domain.ports.in.IOrderModuleService;
import com.forehapp.store.orderModule.infrastructure.web.dto.CancelGroupRequestDto;
import com.forehapp.store.orderModule.infrastructure.web.dto.EditOrderItemsRequestDto;
import com.forehapp.store.orderModule.infrastructure.web.dto.EditOrderItemsResponseDto;
import com.forehapp.store.orderModule.infrastructure.web.dto.OrderItemChangeDto;
import com.forehapp.store.orderModule.infrastructure.web.dto.RemoveShippingCostRequestDto;
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
    private final IOrderItemsEditService itemsEditService;

    public OrderAdminController(IOrderModuleService orderModuleService, IOrderItemsEditService itemsEditService) {
        this.orderModuleService = orderModuleService;
        this.itemsEditService = itemsEditService;
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

    @PatchMapping("/{groupId}/remove-shipping-cost")
    public ResponseEntity<Void> removeShippingCost(
            @PathVariable Long storeId,
            @PathVariable Long groupId,
            @Valid @RequestBody RemoveShippingCostRequestDto dto,
            @AuthenticationPrincipal String userId) {
        orderModuleService.removeShippingCost(storeId, groupId, dto.reason(), Long.parseLong(userId));
        return ResponseEntity.noContent().build();
    }

    /** Replace, re-quantify, re-price, remove or add products before the group ships; emails the buyer. */
    @PutMapping("/{groupId}/items")
    public ResponseEntity<EditOrderItemsResponseDto> editItems(
            @PathVariable Long storeId,
            @PathVariable Long groupId,
            @Valid @RequestBody EditOrderItemsRequestDto dto,
            @AuthenticationPrincipal String userId) {
        return ResponseEntity.ok(itemsEditService.editItems(storeId, groupId, dto, Long.parseLong(userId)));
    }

    @GetMapping("/{groupId}/item-changes")
    public ResponseEntity<List<OrderItemChangeDto>> itemChanges(
            @PathVariable Long storeId,
            @PathVariable Long groupId,
            @AuthenticationPrincipal String userId) {
        return ResponseEntity.ok(itemsEditService.getChanges(storeId, groupId, Long.parseLong(userId)));
    }

    @PatchMapping("/{groupId}/settle-balance")
    public ResponseEntity<Void> settleBalance(
            @PathVariable Long storeId,
            @PathVariable Long groupId,
            @AuthenticationPrincipal String userId) {
        itemsEditService.settleBalance(storeId, groupId, Long.parseLong(userId));
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{groupId}/confirm-payment")
    public ResponseEntity<Void> confirmPayment(
            @PathVariable Long storeId,
            @PathVariable Long groupId,
            @AuthenticationPrincipal String userId) {
        orderModuleService.confirmPayment(storeId, groupId, Long.parseLong(userId));
        return ResponseEntity.noContent().build();
    }
}
