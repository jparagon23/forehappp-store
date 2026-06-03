package com.forehapp.store.authModule.infrastructure.web;

import com.forehapp.store.authModule.application.dto.GoogleLoginRequestDto;
import com.forehapp.store.authModule.application.dto.LoginResponseDto;
import com.forehapp.store.authModule.application.dto.RegisterRequestDto;
import com.forehapp.store.authModule.application.dto.RegisterResponseDto;
import com.forehapp.store.authModule.application.dto.VerifyCodeRequestDto;
import com.forehapp.store.authModule.domain.ports.in.GoogleLoginUseCase;
import com.forehapp.store.authModule.domain.ports.in.GoogleRegisterUseCase;
import com.forehapp.store.authModule.domain.ports.in.RegisterUseCase;
import com.forehapp.store.authModule.domain.ports.in.ResendCodeUseCase;
import com.forehapp.store.authModule.domain.ports.in.VerifyCodeUseCase;
import com.forehapp.store.security.jwt.JwtUtil;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final RegisterUseCase registerUseCase;
    private final VerifyCodeUseCase verifyCodeUseCase;
    private final ResendCodeUseCase resendCodeUseCase;
    private final GoogleLoginUseCase googleLoginUseCase;
    private final GoogleRegisterUseCase googleRegisterUseCase;

    public AuthController(RegisterUseCase registerUseCase,
                          VerifyCodeUseCase verifyCodeUseCase,
                          ResendCodeUseCase resendCodeUseCase,
                          GoogleLoginUseCase googleLoginUseCase,
                          GoogleRegisterUseCase googleRegisterUseCase) {
        this.registerUseCase = registerUseCase;
        this.verifyCodeUseCase = verifyCodeUseCase;
        this.resendCodeUseCase = resendCodeUseCase;
        this.googleLoginUseCase = googleLoginUseCase;
        this.googleRegisterUseCase = googleRegisterUseCase;
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
        Map<String, String> tokens = JwtUtil.refreshToken(refreshToken);
        if (tokens == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(tokens);
    }

    @PostMapping("/google/login")
    public ResponseEntity<LoginResponseDto> googleLogin(@Valid @RequestBody GoogleLoginRequestDto dto) {
        return ResponseEntity.ok(googleLoginUseCase.loginWithGoogle(dto.getIdToken()));
    }

    @PostMapping("/google/register")
    public ResponseEntity<LoginResponseDto> googleRegister(@Valid @RequestBody GoogleLoginRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(googleRegisterUseCase.registerWithGoogle(dto.getIdToken()));
    }
}
