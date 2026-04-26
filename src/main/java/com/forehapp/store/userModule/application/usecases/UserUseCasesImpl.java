package com.forehapp.store.userModule.application.usecases;

import com.forehapp.store.general.exceptions.BadRequestException;
import com.forehapp.store.general.exceptions.NotFoundException;
import com.forehapp.store.userModule.application.dto.ChangePasswordDto;
import com.forehapp.store.userModule.application.dto.UpdateUserRequestDto;
import com.forehapp.store.userModule.application.dto.UserResponse;
import com.forehapp.store.userModule.domain.model.User;
import com.forehapp.store.userModule.domain.ports.in.ChangePasswordUseCase;
import com.forehapp.store.userModule.domain.ports.in.GetUserUseCase;
import com.forehapp.store.userModule.domain.ports.in.UpdateUserUseCase;
import com.forehapp.store.userModule.domain.ports.out.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserUseCasesImpl implements GetUserUseCase, UpdateUserUseCase, ChangePasswordUseCase {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserUseCasesImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserResponse getProfile(Long userId) {
        User user = findOrThrow(userId);
        return new UserResponse(user);
    }

    @Override
    @Transactional
    public UserResponse updateProfile(Long userId, UpdateUserRequestDto dto) {
        User user = findOrThrow(userId);
        user.setName(dto.getName().trim());
        user.setLastname(dto.getLastname().trim());
        return new UserResponse(userRepository.save(user));
    }

    @Override
    @Transactional
    public void changePassword(Long userId, ChangePasswordDto dto) {
        User user = findOrThrow(userId);

        if (!passwordEncoder.matches(dto.getCurrentPassword(), user.getPassword())) {
            throw new BadRequestException("La contraseña actual es incorrecta");
        }
        if (passwordEncoder.matches(dto.getNewPassword(), user.getPassword())) {
            throw new BadRequestException("La nueva contraseña debe ser diferente a la actual");
        }

        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        userRepository.save(user);
    }

    private User findOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));
    }
}
