package com.forehapp.store.orderModule.infrastructure.web;

import com.forehapp.store.orderModule.domain.ports.in.IOrderService;
import com.forehapp.store.orderModule.infrastructure.web.dto.CreateOrderRequestDto;
import com.forehapp.store.orderModule.infrastructure.web.dto.OrderResponse;
import com.forehapp.store.orderModule.infrastructure.web.dto.OrderSummaryDto;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {

    private final IOrderService orderService;

    public OrderController(IOrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(
            @Valid @RequestBody CreateOrderRequestDto dto,
            @AuthenticationPrincipal String userId) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(orderService.createOrder(Long.parseLong(userId), dto));
    }

    @GetMapping
    public ResponseEntity<List<OrderSummaryDto>> getMyOrders(
            @AuthenticationPrincipal String userId) {
        return ResponseEntity.ok(orderService.getMyOrders(Long.parseLong(userId)));
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponse> getOrder(
            @PathVariable Long orderId,
            @AuthenticationPrincipal String userId) {
        return ResponseEntity.ok(orderService.getOrderById(Long.parseLong(userId), orderId));
    }
}
