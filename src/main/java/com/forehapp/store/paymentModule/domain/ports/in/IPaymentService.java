package com.forehapp.store.paymentModule.domain.ports.in;

import com.forehapp.store.orderModule.domain.model.Order;

public interface IPaymentService {
    String createMercadoPagoPreference(Order order);
}
