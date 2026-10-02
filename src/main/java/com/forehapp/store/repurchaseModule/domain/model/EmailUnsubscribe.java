package com.forehapp.store.repurchaseModule.domain.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "store_email_unsubscribes")
@Getter @Setter
@NoArgsConstructor
public class EmailUnsubscribe {

    @Id
    @Column(name = "unsubscribe_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public EmailUnsubscribe(String email) {
        this.email = email;
    }

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
    }
}
