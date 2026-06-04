package com.forehapp.store.orderModule.infrastructure.web;

import com.forehapp.store.authModule.application.dto.LoginResponseDto;
import com.forehapp.store.cartModule.application.dto.ShippingEstimateResponse;
import com.forehapp.store.orderModule.application.usecases.GuestCreateAccountServiceImpl;
import com.forehapp.store.orderModule.domain.ports.in.IGuestCheckoutService;
import com.forehapp.store.orderModule.infrastructure.web.dto.GuestCreateAccountRequestDto;
import com.forehapp.store.orderModule.infrastructure.web.dto.GuestCreateOrderRequestDto;
import com.forehapp.store.orderModule.infrastructure.web.dto.GuestShippingEstimateRequestDto;
import com.forehapp.store.orderModule.infrastructure.web.dto.OrderResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/checkout")
public class GuestCheckoutController {

    private final IGuestCheckoutService guestCheckoutService;
    private final GuestCreateAccountServiceImpl guestCreateAccountService;

    public GuestCheckoutController(IGuestCheckoutService guestCheckoutService,
                                   GuestCreateAccountServiceImpl guestCreateAccountService) {
        this.guestCheckoutService = guestCheckoutService;
        this.guestCreateAccountService = guestCreateAccountService;
    }

    @PostMapping("/guest")
    public ResponseEntity<OrderResponse> placeGuestOrder(@Valid @RequestBody GuestCreateOrderRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(guestCheckoutService.placeOrder(dto));
    }

    @PostMapping("/guest/estimate")
    public ResponseEntity<ShippingEstimateResponse> estimateShipping(
            @Valid @RequestBody GuestShippingEstimateRequestDto dto) {
        return ResponseEntity.ok(guestCheckoutService.estimateShipping(dto));
    }

    @PostMapping("/guest/create-account")
    public ResponseEntity<LoginResponseDto> createAccount(@Valid @RequestBody GuestCreateAccountRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(guestCreateAccountService.createAccount(dto));
    }
}
