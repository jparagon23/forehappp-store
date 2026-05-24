package com.forehapp.store.paymentModule.application.usecases;

import com.forehapp.store.orderModule.domain.model.Order;
import com.forehapp.store.paymentModule.domain.model.Payment;
import com.forehapp.store.paymentModule.domain.model.PaymentMethod;
import com.forehapp.store.paymentModule.domain.model.PaymentStatus;
import com.forehapp.store.paymentModule.domain.ports.in.IPaymentService;
import com.forehapp.store.paymentModule.infrastructure.persistence.IPaymentRepository;
import org.springframework.stereotype.Service;

@Service
public class PaymentServiceImpl implements IPaymentService {

    private final MercadoPagoService mercadoPagoService;
    private final IPaymentRepository paymentRepository;

    public PaymentServiceImpl(MercadoPagoService mercadoPagoService,
                              IPaymentRepository paymentRepository) {
        this.mercadoPagoService = mercadoPagoService;
        this.paymentRepository = paymentRepository;
    }

    @Override
    public String createMercadoPagoPreference(Order order) {
        return mercadoPagoService.createPreference(order);
    }

    @Override
    public void createPendingPayment(Order order, PaymentMethod method) {
        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setMethod(method.name());
        payment.setStatus(PaymentStatus.PENDING.name());
        payment.setAmount(order.getTotal());
        paymentRepository.save(payment);
    }
}
