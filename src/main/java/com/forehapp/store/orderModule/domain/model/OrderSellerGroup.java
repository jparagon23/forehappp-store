package com.forehapp.store.orderModule.domain.model;

import com.forehapp.store.userModule.domain.model.StoreProfile;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "store_order_seller_groups")
@Getter @Setter
@NoArgsConstructor
public class OrderSellerGroup {

    @Id
    @Column(name = "group_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id", nullable = false)
    private StoreProfile seller;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private OrderSellerGroupStatus status = OrderSellerGroupStatus.PENDING;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal subtotal;

    @OneToMany(mappedBy = "sellerGroup", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();
}
