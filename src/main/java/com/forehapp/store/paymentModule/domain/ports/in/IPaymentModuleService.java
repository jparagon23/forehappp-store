package com.forehapp.store.paymentModule.domain.ports.in;

public interface IPaymentModuleService {
    void handlePaymentNotification(String externalPaymentId);
}
