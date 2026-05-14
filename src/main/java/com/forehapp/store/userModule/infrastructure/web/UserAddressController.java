package com.forehapp.store.userModule.infrastructure.web;

import com.forehapp.store.userModule.application.dto.AddressResponse;
import com.forehapp.store.userModule.application.dto.CreateAddressDto;
import com.forehapp.store.userModule.application.dto.UpdateAddressDto;
import com.forehapp.store.userModule.domain.ports.in.*;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users/me/addresses")
public class UserAddressController {

    private final GetAddressesUseCase getAddressesUseCase;
    private final CreateAddressUseCase createAddressUseCase;
    private final UpdateAddressUseCase updateAddressUseCase;
    private final DeleteAddressUseCase deleteAddressUseCase;
    private final SetDefaultAddressUseCase setDefaultAddressUseCase;

    public UserAddressController(GetAddressesUseCase getAddressesUseCase,
                                 CreateAddressUseCase createAddressUseCase,
                                 UpdateAddressUseCase updateAddressUseCase,
                                 DeleteAddressUseCase deleteAddressUseCase,
                                 SetDefaultAddressUseCase setDefaultAddressUseCase) {
        this.getAddressesUseCase = getAddressesUseCase;
        this.createAddressUseCase = createAddressUseCase;
        this.updateAddressUseCase = updateAddressUseCase;
        this.deleteAddressUseCase = deleteAddressUseCase;
        this.setDefaultAddressUseCase = setDefaultAddressUseCase;
    }

    @GetMapping
    public ResponseEntity<List<AddressResponse>> getAddresses(@AuthenticationPrincipal String userId) {
        return ResponseEntity.ok(getAddressesUseCase.getAddresses(Long.parseLong(userId)));
    }

    @PostMapping
    public ResponseEntity<AddressResponse> createAddress(@AuthenticationPrincipal String userId,
                                                         @Valid @RequestBody CreateAddressDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(createAddressUseCase.createAddress(Long.parseLong(userId), dto));
    }

    @PatchMapping("/{addressId}")
    public ResponseEntity<AddressResponse> updateAddress(@AuthenticationPrincipal String userId,
                                                         @PathVariable Long addressId,
                                                         @Valid @RequestBody UpdateAddressDto dto) {
        return ResponseEntity.ok(updateAddressUseCase.updateAddress(Long.parseLong(userId), addressId, dto));
    }

    @DeleteMapping("/{addressId}")
    public ResponseEntity<Void> deleteAddress(@AuthenticationPrincipal String userId,
                                              @PathVariable Long addressId) {
        deleteAddressUseCase.deleteAddress(Long.parseLong(userId), addressId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{addressId}/default")
    public ResponseEntity<AddressResponse> setDefault(@AuthenticationPrincipal String userId,
                                                      @PathVariable Long addressId) {
        return ResponseEntity.ok(setDefaultAddressUseCase.setDefault(Long.parseLong(userId), addressId));
    }
}
