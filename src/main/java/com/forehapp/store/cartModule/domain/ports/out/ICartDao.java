package com.forehapp.store.cartModule.domain.ports.out;

import com.forehapp.store.cartModule.domain.model.Cart;

import java.util.Optional;

public interface ICartDao {
    Optional<Cart> findActiveByBuyerId(Long buyerId);
    Cart save(Cart cart);
    void delete(Cart cart);
}
