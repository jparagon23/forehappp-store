package com.forehapp.store.returnModule.domain.model;

import com.forehapp.store.orderModule.domain.model.OrderItem;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "store_return_items")
@Getter @Setter
@NoArgsConstructor
public class ReturnItem {

    @Id
    @Column(name = "return_item_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "return_id", nullable = false)
    private ReturnRequest returnRequest;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_item_id", nullable = false)
    private OrderItem orderItem;

    @Column(name = "quantity_to_return", nullable = false)
    private Integer quantityToReturn;
}
