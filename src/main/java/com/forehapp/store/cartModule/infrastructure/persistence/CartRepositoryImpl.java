package com.forehapp.store.cartModule.infrastructure.persistence;

import com.forehapp.store.cartModule.domain.model.Cart;
import com.forehapp.store.cartModule.domain.model.CartStatus;
import com.forehapp.store.cartModule.domain.ports.out.ICartDao;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public class CartRepositoryImpl implements ICartDao {

    private final ICartRepository jpaRepository;

    public CartRepositoryImpl(ICartRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Optional<Cart> findActiveByBuyerId(Long buyerId) {
        return jpaRepository.findByBuyerIdAndStatus(buyerId, CartStatus.ACTIVE);
    }

    @Override
    public Cart save(Cart cart) {
        return jpaRepository.save(cart);
    }

    @Override
    public void delete(Cart cart) {
        jpaRepository.delete(cart);
    }

    @Override
    public int expireOldCarts(LocalDateTime threshold) {
        return jpaRepository.expireOldCarts(threshold);
    }
}
