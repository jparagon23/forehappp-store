package com.forehapp.store.authModule.application.usecases;

import com.forehapp.store.authModule.application.dto.*;
import com.forehapp.store.authModule.application.services.ConfirmationTokenService;
import com.forehapp.store.authModule.domain.model.ConfirmationToken;
import com.forehapp.store.authModule.domain.ports.in.RegisterUseCase;
import com.forehapp.store.authModule.domain.ports.in.ResendCodeUseCase;
import com.forehapp.store.authModule.domain.ports.in.VerifyCodeUseCase;
import com.forehapp.store.general.constants.Constants;
import com.forehapp.store.general.exceptions.BadRequestException;
import com.forehapp.store.mail.EmailSender;
import com.forehapp.store.security.config.UserDetailsImpl;
import com.forehapp.store.security.jwt.JwtUtil;
import com.forehapp.store.userModule.domain.model.Role;
import com.forehapp.store.userModule.domain.model.User;
import com.forehapp.store.userModule.domain.ports.out.RoleRepository;
import com.forehapp.store.userModule.domain.ports.out.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AuthUseCasesImpl implements RegisterUseCase, VerifyCodeUseCase, ResendCodeUseCase {

    private static final Logger logger = LoggerFactory.getLogger(AuthUseCasesImpl.class);

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final ConfirmationTokenService confirmationTokenService;
    private final EmailSender emailSender;

    public AuthUseCasesImpl(UserRepository userRepository,
                            RoleRepository roleRepository,
                            PasswordEncoder passwordEncoder,
                            ConfirmationTokenService confirmationTokenService,
                            EmailSender emailSender) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.confirmationTokenService = confirmationTokenService;
        this.emailSender = emailSender;
    }

    @Override
    @Transactional
    public RegisterResponseDto register(RegisterRequestDto dto) {
        String email = dto.getEmail().trim().toLowerCase();

        if (userRepository.findByEmail(email).isPresent()) {
            throw new BadRequestException("El email ya está registrado");
        }

        Role userRole = roleRepository.findById((long) Constants.USER_ROLE_ID)
                .orElseThrow(() -> new IllegalStateException("Default user role not found"));

        User user = new User();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setName(dto.getName().trim());
        user.setLastname(dto.getLastname().trim());
        user.setUserStatus(Constants.PENDING_STATUS);
        user.setAllowNotification("T");
        user.setRoles(List.of(userRole));

        User saved = userRepository.save(user);
        logger.info("User registered (pending) with id: {}", saved.getId());

        sendVerificationCode(saved);

        return new RegisterResponseDto(saved.getId(), "Código de verificación enviado a " + email);
    }

    @Override
    @Transactional
    public LoginResponseDto verifyCode(VerifyCodeRequestDto dto) {
        ConfirmationToken token = confirmationTokenService.findByCode(dto.getCode())
                .orElseThrow(() -> new BadRequestException("Código inválido"));

        if (token.getConfirmedAt() != null) {
            throw new BadRequestException("El código ya fue utilizado");
        }
        if (!token.getUser().getId().equals(dto.getUserId())) {
            throw new BadRequestException("Código inválido");
        }
        if (token.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("El código expiró. Solicitá uno nuevo");
        }

        confirmationTokenService.markAsConfirmed(dto.getCode());

        User user = token.getUser();
        user.setUserStatus(Constants.ACTIVE_USER_STATUS);
        userRepository.save(user);
        logger.info("User {} verified and activated", user.getId());

        UserDetailsImpl userDetails = new UserDetailsImpl(user);
        String accessToken = JwtUtil.createToken(String.valueOf(user.getId()), userDetails.getAuthorities());
        String refreshToken = JwtUtil.createRefreshToken(String.valueOf(user.getId()), userDetails.getAuthorities());

        return new LoginResponseDto(accessToken, refreshToken, user.getId(), user.getName(), user.getEmail());
    }

    @Override
    @Transactional
    public void resendCode(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BadRequestException("Usuario no encontrado"));

        if (user.getUserStatus() == Constants.ACTIVE_USER_STATUS) {
            throw new BadRequestException("La cuenta ya está verificada");
        }

        sendVerificationCode(user);
        logger.info("Verification code resent to user {}", userId);
    }

    private void sendVerificationCode(User user) {
        ConfirmationToken token = confirmationTokenService.createToken(user);
        String html = buildVerificationEmail(user.getName(), token.getToken());
        emailSender.sendEmail(user.getEmail(), "Verificá tu cuenta en Forehapp", html);
    }

    private String buildVerificationEmail(String name, String code) {
        return "<div style='font-family:sans-serif;max-width:480px;margin:auto'>"
                + "<h2>Hola, " + name + "</h2>"
                + "<p>Tu código de verificación es:</p>"
                + "<div style='font-size:36px;font-weight:bold;letter-spacing:8px;text-align:center;"
                + "padding:16px;background:#f4f4f4;border-radius:8px'>" + code + "</div>"
                + "<p style='color:#666;font-size:13px'>Válido por 15 minutos. No lo compartas con nadie.</p>"
                + "</div>";
    }
}
