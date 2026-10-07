package com.forehapp.store.orderModule.infrastructure.web;

import com.forehapp.store.orderModule.domain.ports.in.IAssistedOrderService;
import com.forehapp.store.orderModule.infrastructure.web.dto.AssistedCouponValidateDto;
import com.forehapp.store.orderModule.infrastructure.web.dto.AssistedCustomerResponse;
import com.forehapp.store.orderModule.infrastructure.web.dto.AssistedOrderRequestDto;
import com.forehapp.store.orderModule.infrastructure.web.dto.OrderResponse;
import com.forehapp.store.promotionModule.application.dto.CouponValidationResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/stores/{storeId}/assisted-orders")
public class AssistedOrderController {

    private final IAssistedOrderService assistedOrderService;

    public AssistedOrderController(IAssistedOrderService assistedOrderService) {
        this.assistedOrderService = assistedOrderService;
    }

    /** checkoutUrl in the response is the MercadoPago link to send the customer (null for other methods). */
    @PostMapping
    public ResponseEntity<OrderResponse> placeOrder(@PathVariable Long storeId,
                                                    @Valid @RequestBody AssistedOrderRequestDto dto,
                                                    @AuthenticationPrincipal String userId) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(assistedOrderService.placeOrder(storeId, dto, Long.parseLong(userId)));
    }

    @PostMapping("/coupon/validate")
    public ResponseEntity<CouponValidationResponse> validateCoupon(@PathVariable Long storeId,
                                                                   @Valid @RequestBody AssistedCouponValidateDto dto,
                                                                   @AuthenticationPrincipal String userId) {
        return ResponseEntity.ok(assistedOrderService.validateCoupon(storeId, dto, Long.parseLong(userId)));
    }

    @GetMapping("/customer")
    public ResponseEntity<AssistedCustomerResponse> lookupCustomer(@PathVariable Long storeId,
                                                                   @RequestParam String email,
                                                                   @AuthenticationPrincipal String userId) {
        return ResponseEntity.ok(assistedOrderService.lookupCustomer(storeId, email, Long.parseLong(userId)));
    }
}
