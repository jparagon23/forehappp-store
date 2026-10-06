package com.forehapp.store.paymentModule.domain.ports.in;

public interface IPaymentModuleService {
    void handlePaymentNotification(String externalPaymentId);
    void confirmCashPayment(Long userId, Long orderId);

    /**
     * Marks a PENDING CASH or TRANSFER order as paid, without an access check (callers do it).
     * notifyBuyer=false skips the "payment confirmed" email, for orders born paid whose
     * confirmation email already says so.
     */
    void confirmManualPayment(Long orderId, boolean notifyBuyer);
}
