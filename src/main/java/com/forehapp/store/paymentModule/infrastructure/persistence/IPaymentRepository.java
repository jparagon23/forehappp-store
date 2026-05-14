package com.forehapp.store.paymentModule.infrastructure.persistence;

import com.forehapp.store.paymentModule.domain.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface IPaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByOrderId(Long orderId);
}
