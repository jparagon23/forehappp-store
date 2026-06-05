package com.forehapp.store.orderModule.domain.model;

import com.forehapp.store.userModule.domain.model.StoreProfile;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "store_orders")
@Getter @Setter
@NoArgsConstructor
public class Order {

    @Id
    @Column(name = "order_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "buyer_id", nullable = true)
    private StoreProfile buyer;

    @Column(name = "guest_name", length = 100)
    private String guestName;

    @Column(name = "guest_lastname", length = 100)
    private String guestLastname;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private OrderStatus status = OrderStatus.PENDING;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal total;

    @Column(name = "buyer_phone", length = 50)
    private String buyerPhone;

    @Column(name = "buyer_email", length = 150)
    private String buyerEmail;

    @Column(name = "shipping_address", nullable = false, length = 255)
    private String shippingAddress;

    @Column(name = "shipping_city", nullable = false, length = 100)
    private String shippingCity;

    @Column(name = "shipping_department", length = 100)
    private String shippingDepartment;

    @Column(name = "shipping_country", nullable = false, length = 100)
    private String shippingCountry;

    @Column(name = "shipping_complement", length = 255)
    private String shippingComplement;

    @Column(name = "shipping_reference", length = 255)
    private String shippingReference;

    @Column(name = "shipping_city_id")
    private Long shippingCityId;

    @Column(name = "payment_method", nullable = false, length = 30)
    private String paymentMethod;

    @Column(name = "coupon_code", length = 50)
    private String couponCode;

    @Column(name = "coupon_discount", precision = 14, scale = 2)
    private BigDecimal couponDiscount;

    @Column(name = "mercado_pago_surcharge", precision = 14, scale = 2)
    private BigDecimal mercadoPagoSurcharge;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderSellerGroup> sellerGroups = new ArrayList<>();

    public String resolveContactName() {
        if (buyer != null) {
            return buyer.getUser().getName() + " " + buyer.getUser().getLastname();
        }
        return guestName + " " + guestLastname;
    }

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
    }
}
