package com.forehapp.store.cartModule.domain.ports.in;

import com.forehapp.store.cartModule.application.dto.AddItemRequestDto;
import com.forehapp.store.cartModule.application.dto.CartResponse;
import com.forehapp.store.cartModule.application.dto.UpdateCartItemDto;

import java.util.List;

public interface ICartService {
    CartResponse getCart(Long userId);
    CartResponse addItem(Long userId, AddItemRequestDto dto);
    CartResponse addItems(Long userId, List<AddItemRequestDto> dtos);
    CartResponse updateItem(Long userId, Long itemId, UpdateCartItemDto dto);
    void removeItem(Long userId, Long itemId);
    void clearCart(Long userId);
}
