package com.forehapp.store.paymentModule.infrastructure.web;

import com.forehapp.store.paymentModule.domain.ports.in.IPaymentModuleService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/payments")
public class PaymentController {

    private final IPaymentModuleService paymentModuleService;

    public PaymentController(IPaymentModuleService paymentModuleService) {
        this.paymentModuleService = paymentModuleService;
    }

    @PatchMapping("/orders/{orderId}/confirm-cash")
    public ResponseEntity<Void> confirmCashPayment(
            @PathVariable Long orderId,
            @AuthenticationPrincipal String userId) {
        paymentModuleService.confirmCashPayment(Long.parseLong(userId), orderId);
        return ResponseEntity.noContent().build();
    }
}
