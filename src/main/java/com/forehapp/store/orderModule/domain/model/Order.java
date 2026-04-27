package com.forehapp.store.orderModule.domain.model;

import com.forehapp.store.userModule.domain.model.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "orders")
@Getter @Setter
@NoArgsConstructor
public class Order {

    @Id
    @Column(name = "order_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "status_id", nullable = false)
    private OrderStatus status;

    @Column(name = "order_date", nullable = false, columnDefinition = "TIMESTAMP")
    private LocalDateTime orderDate;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal total;

    @PrePersist
    public void prePersist() {
        orderDate = LocalDateTime.now();
    }
}
