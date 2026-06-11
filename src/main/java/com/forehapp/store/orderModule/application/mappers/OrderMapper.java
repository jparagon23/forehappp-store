package com.forehapp.store.orderModule.application.mappers;

import com.forehapp.store.orderModule.domain.model.Order;
import com.forehapp.store.orderModule.domain.model.OrderItem;
import com.forehapp.store.orderModule.domain.model.OrderSellerGroup;
import com.forehapp.store.orderModule.domain.model.OrderSellerGroupStatus;
import com.forehapp.store.orderModule.infrastructure.web.dto.OrderItemDto;
import com.forehapp.store.orderModule.infrastructure.web.dto.OrderResponse;
import com.forehapp.store.orderModule.infrastructure.web.dto.OrderSellerGroupDto;
import com.forehapp.store.orderModule.infrastructure.web.dto.OrderSummaryDto;
import com.forehapp.store.orderModule.infrastructure.web.dto.VariantAttributeDto;
import com.forehapp.store.productModule.domain.model.Product;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Component
public class OrderMapper {

    private static final Map<OrderSellerGroupStatus, Integer> STATUS_PRIORITY = Map.of(
            OrderSellerGroupStatus.DELIVERED,  5,
            OrderSellerGroupStatus.SHIPPED,    4,
            OrderSellerGroupStatus.PREPARING,  3,
            OrderSellerGroupStatus.PENDING,    2,
            OrderSellerGroupStatus.CANCELLED,  1
    );

    public OrderResponse toResponse(Order order, String checkoutUrl) {
        List<OrderSellerGroupDto> groups = order.getSellerGroups().stream()
                .map(this::toGroupDto)
                .toList();

        BigDecimal subtotal = order.getSellerGroups().stream()
                .map(OrderSellerGroup::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal shippingTotal = order.getSellerGroups().stream()
                .map(OrderSellerGroup::getShippingCost)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new OrderResponse(
                order.getId(),
                order.getStatus().name(),
                order.getPaymentMethod(),
                subtotal,
                shippingTotal,
                order.getCouponCode(),
                order.getCouponDiscount(),
                order.getMercadoPagoSurcharge(),
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
        String shippingStatus = order.getSellerGroups().stream()
                .map(OrderSellerGroup::getStatus)
                .max(Comparator.comparingInt(s -> STATUS_PRIORITY.getOrDefault(s, 0)))
                .map(OrderSellerGroupStatus::name)
                .orElse(OrderSellerGroupStatus.PENDING.name());
        return new OrderSummaryDto(
                order.getId(),
                order.getStatus().name(),
                order.getPaymentMethod(),
                shippingStatus,
                order.getTotal(),
                order.getCreatedAt(),
                order.getSellerGroups().size()
        );
    }

    private OrderSellerGroupDto toGroupDto(OrderSellerGroup group) {
        List<OrderItemDto> items = group.getItems().stream()
                .map(this::toItemDto)
                .toList();
        return new OrderSellerGroupDto(
                group.getId(),
                group.getStore().getId(),
                group.getStore().getName(),
                group.getStatus().name(),
                group.getSubtotal(),
                group.getShippingCost(),
                group.getTrackingNumber(),
                group.getPreparedAt(),
                group.getShippedAt(),
                group.getDeliveredAt(),
                items
        );
    }

    private OrderItemDto toItemDto(OrderItem item) {
        BigDecimal subtotal = item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
        List<VariantAttributeDto> attributes = item.getVariant().getAttributeValues().stream()
                .map(av -> new VariantAttributeDto(
                        av.getAttribute().getDescription(),
                        av.getDescription()))
                .toList();
        Product product = item.getVariant().getProduct();
        return new OrderItemDto(
                item.getId(),
                item.getVariant().getId(),
                item.getVariant().getSku(),
                product.getTitle(),
                product.getCategory().getDescription(),
                product.getLine() != null ? product.getLine().getDescription() : null,
                attributes,
                item.getQuantity(),
                item.getUnitPrice(),
                subtotal
        );
    }
}
