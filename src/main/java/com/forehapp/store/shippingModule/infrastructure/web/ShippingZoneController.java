package com.forehapp.store.shippingModule.infrastructure.web;

import com.forehapp.store.shippingModule.domain.ports.in.IShippingZoneService;
import com.forehapp.store.shippingModule.infrastructure.web.dto.CreateShippingZoneDto;
import com.forehapp.store.shippingModule.infrastructure.web.dto.ShippingZoneResponse;
import com.forehapp.store.shippingModule.infrastructure.web.dto.UpdateShippingZoneDto;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/shipping-zones")
public class ShippingZoneController {

    private final IShippingZoneService zoneService;

    public ShippingZoneController(IShippingZoneService zoneService) {
        this.zoneService = zoneService;
    }

    @PostMapping
    public ResponseEntity<ShippingZoneResponse> create(
            @Valid @RequestBody CreateShippingZoneDto dto,
            @AuthenticationPrincipal String userId) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(zoneService.create(dto, Long.parseLong(userId)));
    }

    @GetMapping
    public ResponseEntity<List<ShippingZoneResponse>> getAll(@AuthenticationPrincipal String userId) {
        return ResponseEntity.ok(zoneService.getAll(Long.parseLong(userId)));
    }

    @GetMapping("/{zoneId}")
    public ResponseEntity<ShippingZoneResponse> getById(
            @PathVariable Long zoneId,
            @AuthenticationPrincipal String userId) {
        return ResponseEntity.ok(zoneService.getById(zoneId, Long.parseLong(userId)));
    }

    @PatchMapping("/{zoneId}")
    public ResponseEntity<ShippingZoneResponse> update(
            @PathVariable Long zoneId,
            @Valid @RequestBody UpdateShippingZoneDto dto,
            @AuthenticationPrincipal String userId) {
        return ResponseEntity.ok(zoneService.update(zoneId, dto, Long.parseLong(userId)));
    }

    @DeleteMapping("/{zoneId}")
    public ResponseEntity<Void> delete(
            @PathVariable Long zoneId,
            @AuthenticationPrincipal String userId) {
        zoneService.delete(zoneId, Long.parseLong(userId));
        return ResponseEntity.noContent().build();
    }
}
