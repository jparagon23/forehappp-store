package com.forehapp.store.shippingModule.domain.model;

import com.forehapp.store.orderModule.domain.model.Order;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "shipments")
@Getter @Setter
@NoArgsConstructor
public class Shipment {

    @Id
    @Column(name = "shipment_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(length = 255)
    private String address;

    @Column(length = 100)
    private String city;

    @Column(length = 100)
    private String country;

    @Column(name = "shipment_status", nullable = false, length = 50)
    private String shipmentStatus;

    @Column(name = "ship_date")
    private LocalDate shipDate;

    @Column(name = "delivery_date")
    private LocalDate deliveryDate;
}
