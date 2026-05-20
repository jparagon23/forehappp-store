package com.forehapp.store.storeModule.infrastructure.web;

import com.forehapp.store.storeModule.application.dto.*;
import com.forehapp.store.storeModule.domain.ports.in.IStoreService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/stores")
public class StoreController {

    private final IStoreService storeService;

    public StoreController(IStoreService storeService) {
        this.storeService = storeService;
    }

    @PostMapping
    public ResponseEntity<StoreResponse> createStore(
            @AuthenticationPrincipal String userId,
            @Valid @RequestBody CreateStoreRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(storeService.createStore(dto, Long.parseLong(userId)));
    }

    @GetMapping("/my")
    public ResponseEntity<List<MyStoreResponse>> getMyStores(
            @AuthenticationPrincipal String userId) {
        return ResponseEntity.ok(storeService.getMyStores(Long.parseLong(userId)));
    }

    @GetMapping("/{storeId}")
    public ResponseEntity<StoreResponse> getStore(
            @AuthenticationPrincipal String userId,
            @PathVariable Long storeId) {
        return ResponseEntity.ok(storeService.getStore(storeId, Long.parseLong(userId)));
    }

    @PatchMapping("/{storeId}")
    public ResponseEntity<StoreResponse> updateStore(
            @AuthenticationPrincipal String userId,
            @PathVariable Long storeId,
            @Valid @RequestBody UpdateStoreRequestDto dto) {
        return ResponseEntity.ok(storeService.updateStore(storeId, dto, Long.parseLong(userId)));
    }

    @GetMapping("/{storeId}/members")
    public ResponseEntity<List<StoreMembershipResponse>> getMembers(
            @AuthenticationPrincipal String userId,
            @PathVariable Long storeId) {
        return ResponseEntity.ok(storeService.getMembers(storeId, Long.parseLong(userId)));
    }

    @PostMapping("/{storeId}/members")
    public ResponseEntity<StoreMembershipResponse> inviteMember(
            @AuthenticationPrincipal String userId,
            @PathVariable Long storeId,
            @Valid @RequestBody InviteMemberRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(storeService.inviteMember(storeId, dto, Long.parseLong(userId)));
    }

    @PatchMapping("/{storeId}/members/{membershipId}/role")
    public ResponseEntity<StoreMembershipResponse> updateMemberRole(
            @AuthenticationPrincipal String userId,
            @PathVariable Long storeId,
            @PathVariable Long membershipId,
            @Valid @RequestBody UpdateMemberRoleRequestDto dto) {
        return ResponseEntity.ok(storeService.updateMemberRole(storeId, membershipId, dto, Long.parseLong(userId)));
    }

    @DeleteMapping("/{storeId}/members/{membershipId}")
    public ResponseEntity<Void> removeMember(
            @AuthenticationPrincipal String userId,
            @PathVariable Long storeId,
            @PathVariable Long membershipId) {
        storeService.removeMember(storeId, membershipId, Long.parseLong(userId));
        return ResponseEntity.noContent().build();
    }
}
