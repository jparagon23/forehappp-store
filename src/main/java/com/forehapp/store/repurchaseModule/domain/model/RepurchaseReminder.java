package com.forehapp.store.repurchaseModule.domain.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "store_repurchase_reminders")
@Getter @Setter
@NoArgsConstructor
public class RepurchaseReminder {

    @Id
    @Column(name = "reminder_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_item_id", nullable = false, unique = true)
    private Long orderItemId;

    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(nullable = false, length = 150)
    private String email;

    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @Column(name = "sent_at", nullable = false)
    private LocalDateTime sentAt;
}
