package com.forehapp.store.orderModule.application.mappers;

import com.forehapp.store.orderModule.domain.model.Order;
import com.forehapp.store.orderModule.domain.model.OrderItem;
import com.forehapp.store.orderModule.domain.model.OrderSellerGroup;
import com.forehapp.store.orderModule.infrastructure.web.dto.OrderItemDto;
import com.forehapp.store.orderModule.infrastructure.web.dto.OrderResponse;
import com.forehapp.store.orderModule.infrastructure.web.dto.OrderSellerGroupDto;
import com.forehapp.store.orderModule.infrastructure.web.dto.OrderSummaryDto;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
public class OrderMapper {

    public OrderResponse toResponse(Order order, String checkoutUrl) {
        List<OrderSellerGroupDto> groups = order.getSellerGroups().stream()
                .map(this::toGroupDto)
                .toList();
        return new OrderResponse(
                order.getId(),
                order.getStatus().name(),
                order.getTotal(),
                order.getShippingAddress(),
                order.getShippingCity(),
                order.getShippingCountry(),
                order.getCreatedAt(),
                groups,
                checkoutUrl
        );
    }

    public OrderSummaryDto toSummary(Order order) {
        return new OrderSummaryDto(
                order.getId(),
                order.getStatus().name(),
                order.getTotal(),
                order.getCreatedAt(),
                order.getSellerGroups().size()
        );
    }

    private OrderSellerGroupDto toGroupDto(OrderSellerGroup group) {
        String sellerName = group.getSeller().getUser().getName() + " " + group.getSeller().getUser().getLastname();
        List<OrderItemDto> items = group.getItems().stream()
                .map(this::toItemDto)
                .toList();
        return new OrderSellerGroupDto(
                group.getId(),
                group.getSeller().getId(),
                sellerName,
                group.getStatus().name(),
                group.getSubtotal(),
                items
        );
    }

    private OrderItemDto toItemDto(OrderItem item) {
        BigDecimal subtotal = item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
        return new OrderItemDto(
                item.getId(),
                item.getVariant().getId(),
                item.getVariant().getSku(),
                item.getVariant().getProduct().getTitle(),
                item.getQuantity(),
                item.getUnitPrice(),
                subtotal
        );
    }
}
