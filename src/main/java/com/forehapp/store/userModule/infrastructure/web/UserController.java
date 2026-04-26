package com.forehapp.store.userModule.infrastructure.web;

import com.forehapp.store.userModule.application.dto.ChangePasswordDto;
import com.forehapp.store.userModule.application.dto.UpdateUserRequestDto;
import com.forehapp.store.userModule.application.dto.UserResponse;
import com.forehapp.store.userModule.domain.ports.in.ChangePasswordUseCase;
import com.forehapp.store.userModule.domain.ports.in.GetUserUseCase;
import com.forehapp.store.userModule.domain.ports.in.UpdateUserUseCase;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final GetUserUseCase getUserUseCase;
    private final UpdateUserUseCase updateUserUseCase;
    private final ChangePasswordUseCase changePasswordUseCase;

    public UserController(GetUserUseCase getUserUseCase,
                          UpdateUserUseCase updateUserUseCase,
                          ChangePasswordUseCase changePasswordUseCase) {
        this.getUserUseCase = getUserUseCase;
        this.updateUserUseCase = updateUserUseCase;
        this.changePasswordUseCase = changePasswordUseCase;
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getProfile(@AuthenticationPrincipal String userId) {
        return ResponseEntity.ok(getUserUseCase.getProfile(Long.parseLong(userId)));
    }

    @PatchMapping("/me")
    public ResponseEntity<UserResponse> updateProfile(@AuthenticationPrincipal String userId,
                                                      @Valid @RequestBody UpdateUserRequestDto dto) {
        return ResponseEntity.ok(updateUserUseCase.updateProfile(Long.parseLong(userId), dto));
    }

    @PatchMapping("/me/password")
    public ResponseEntity<Void> changePassword(@AuthenticationPrincipal String userId,
                                               @Valid @RequestBody ChangePasswordDto dto) {
        changePasswordUseCase.changePassword(Long.parseLong(userId), dto);
        return ResponseEntity.ok().build();
    }
}
