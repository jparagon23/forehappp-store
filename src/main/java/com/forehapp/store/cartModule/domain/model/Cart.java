package com.forehapp.store.cartModule.domain.model;

import com.forehapp.store.userModule.domain.model.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "carts")
@Getter @Setter
@NoArgsConstructor
public class Cart {

    @Id
    @Column(name = "cart_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "creation_date", nullable = false, columnDefinition = "TIMESTAMP")
    private LocalDateTime creationDate;

    @Column(nullable = false, length = 50)
    private String status;

    @PrePersist
    public void prePersist() {
        creationDate = LocalDateTime.now();
    }
}
