package com.forehapp.store.authModule.infrastructure.web;

import com.forehapp.store.authModule.application.dto.GoogleLoginRequestDto;
import com.forehapp.store.authModule.application.dto.LoginResponseDto;
import com.forehapp.store.authModule.application.dto.RegisterRequestDto;
import com.forehapp.store.authModule.application.dto.RegisterResponseDto;
import com.forehapp.store.authModule.application.dto.VerifyCodeRequestDto;
import com.forehapp.store.authModule.application.services.AuthSessionService;
import com.forehapp.store.authModule.domain.ports.in.GoogleLoginUseCase;
import com.forehapp.store.authModule.domain.ports.in.GoogleRegisterUseCase;
import com.forehapp.store.authModule.domain.ports.in.RegisterUseCase;
import com.forehapp.store.authModule.domain.ports.in.ResendCodeUseCase;
import com.forehapp.store.authModule.domain.ports.in.VerifyCodeUseCase;
import com.forehapp.store.userModule.domain.ports.out.UserRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Validated
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final RegisterUseCase registerUseCase;
    private final VerifyCodeUseCase verifyCodeUseCase;
    private final ResendCodeUseCase resendCodeUseCase;
    private final GoogleLoginUseCase googleLoginUseCase;
    private final GoogleRegisterUseCase googleRegisterUseCase;
    private final UserRepository userRepository;
    private final AuthSessionService authSessionService;

    public AuthController(RegisterUseCase registerUseCase,
                          VerifyCodeUseCase verifyCodeUseCase,
                          ResendCodeUseCase resendCodeUseCase,
                          GoogleLoginUseCase googleLoginUseCase,
                          GoogleRegisterUseCase googleRegisterUseCase,
                          UserRepository userRepository,
                          AuthSessionService authSessionService) {
        this.registerUseCase = registerUseCase;
        this.verifyCodeUseCase = verifyCodeUseCase;
        this.resendCodeUseCase = resendCodeUseCase;
        this.googleLoginUseCase = googleLoginUseCase;
        this.googleRegisterUseCase = googleRegisterUseCase;
        this.userRepository = userRepository;
        this.authSessionService = authSessionService;
    }

    @PostMapping("/register")
    public ResponseEntity<RegisterResponseDto> register(@Valid @RequestBody RegisterRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(registerUseCase.register(dto));
    }

    @PostMapping("/verify-code")
    public ResponseEntity<LoginResponseDto> verifyCode(@Valid @RequestBody VerifyCodeRequestDto dto) {
        return ResponseEntity.ok(verifyCodeUseCase.verifyCode(dto));
    }

    @PostMapping("/resend-code")
    public ResponseEntity<Void> resendCode(@RequestBody Map<String, Long> body) {
        resendCodeUseCase.resendCode(body.get("userId"));
        return ResponseEntity.ok().build();
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<Map<String, String>> refreshToken(@RequestBody Map<String, String> body) {
        String refreshToken = body.get("refreshToken");
        if (refreshToken == null) {
            return ResponseEntity.badRequest().build();
        }
        return authSessionService.refresh(refreshToken)
                .map(tokens -> ResponseEntity.ok(Map.of(
                        "access_token", tokens.accessToken(),
                        "refresh_token", tokens.refreshToken())))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
    }

    /** Closes the login the refresh token belongs to; its tokens stop working. */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestBody Map<String, String> body) {
        String refreshToken = body.get("refreshToken");
        if (refreshToken != null) authSessionService.close(refreshToken);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/google/login")
    public ResponseEntity<LoginResponseDto> googleLogin(@Valid @RequestBody GoogleLoginRequestDto dto) {
        return ResponseEntity.ok(googleLoginUseCase.loginWithGoogle(dto.getIdToken()));
    }

    @PostMapping("/google/register")
    public ResponseEntity<LoginResponseDto> googleRegister(@Valid @RequestBody GoogleLoginRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(googleRegisterUseCase.registerWithGoogle(dto.getIdToken()));
    }

    @GetMapping("/check-email")
    public ResponseEntity<Map<String, Boolean>> checkEmail(
            @RequestParam @NotBlank @Email String email) {
        boolean exists = userRepository.findByEmail(email).isPresent();
        return ResponseEntity.ok(Map.of("exists", exists));
    }
}
