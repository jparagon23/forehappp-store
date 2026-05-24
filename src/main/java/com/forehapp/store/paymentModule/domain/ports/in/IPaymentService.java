package com.forehapp.store.paymentModule.domain.ports.in;

import com.forehapp.store.orderModule.domain.model.Order;
import com.forehapp.store.paymentModule.domain.model.PaymentMethod;

public interface IPaymentService {
    String createMercadoPagoPreference(Order order);
    void createPendingPayment(Order order, PaymentMethod method);
}
