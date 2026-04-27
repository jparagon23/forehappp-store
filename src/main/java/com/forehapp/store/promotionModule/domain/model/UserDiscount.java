package com.forehapp.store.promotionModule.domain.model;

import com.forehapp.store.userModule.domain.model.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "user_discounts",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "discount_id"}))
@Getter @Setter
@NoArgsConstructor
public class UserDiscount {

    @Id
    @Column(name = "user_discount_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "discount_id", nullable = false)
    private Discount discount;
}
