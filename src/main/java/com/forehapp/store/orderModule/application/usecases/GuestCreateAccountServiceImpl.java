package com.forehapp.store.orderModule.application.usecases;

import com.forehapp.store.authModule.application.dto.RegisterRequestDto;
import com.forehapp.store.authModule.application.dto.RegisterResponseDto;
import com.forehapp.store.authModule.domain.ports.in.RegisterUseCase;
import com.forehapp.store.orderModule.domain.model.Order;
import com.forehapp.store.orderModule.domain.ports.out.IOrderDao;
import com.forehapp.store.orderModule.infrastructure.web.dto.GuestCreateAccountRequestDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * "Save your data" after a guest checkout. The account is created PENDING and a code is sent to the
 * email: guest orders are linked only after the code is verified (POST /auth/verify-code), so knowing
 * someone's email is not enough to take over their orders and addresses.
 */
@Service
public class GuestCreateAccountServiceImpl {

    private final IOrderDao orderDao;
    private final RegisterUseCase registerUseCase;

    public GuestCreateAccountServiceImpl(IOrderDao orderDao, RegisterUseCase registerUseCase) {
        this.orderDao = orderDao;
        this.registerUseCase = registerUseCase;
    }

    @Transactional
    public RegisterResponseDto createAccount(GuestCreateAccountRequestDto dto) {
        String email = dto.email().trim().toLowerCase();

        // Name comes from the latest guest order; register() rejects emails that already have an account
        List<Order> guestOrders = orderDao.findGuestOrdersByEmail(email);
        Order latest = guestOrders.isEmpty() ? null : guestOrders.get(0);

        RegisterRequestDto register = new RegisterRequestDto();
        register.setEmail(email);
        register.setPassword(dto.password());
        register.setName(latest != null && latest.getGuestName() != null ? latest.getGuestName() : email.split("@")[0]);
        register.setLastname(latest != null && latest.getGuestLastname() != null ? latest.getGuestLastname() : "");
        return registerUseCase.register(register);
    }
}
