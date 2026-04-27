package com.forehapp.store.paymentModule.domain.model;

import com.forehapp.store.orderModule.domain.model.Order;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Getter @Setter
@NoArgsConstructor
public class Payment {

    @Id
    @Column(name = "payment_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(nullable = false, length = 100)
    private String method;

    @Column(nullable = false, length = 50)
    private String status;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Column(name = "payment_date", nullable = false, columnDefinition = "TIMESTAMP")
    private LocalDateTime paymentDate;

    @Column(length = 255)
    private String reference;

    @PrePersist
    public void prePersist() {
        paymentDate = LocalDateTime.now();
    }
}
