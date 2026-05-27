package com.forehapp.store.userModule.application.usecases;

import com.forehapp.store.general.exceptions.BadRequestException;
import com.forehapp.store.general.exceptions.ErrorCode;
import com.forehapp.store.general.exceptions.NotFoundException;
import com.forehapp.store.userModule.application.dto.ChangePasswordDto;
import com.forehapp.store.userModule.application.dto.UpdatePhoneRequestDto;
import com.forehapp.store.userModule.application.dto.UpdateUserRequestDto;
import com.forehapp.store.userModule.application.dto.UserResponse;
import com.forehapp.store.userModule.application.dto.UserSearchResponse;
import com.forehapp.store.userModule.domain.model.StoreProfile;
import com.forehapp.store.userModule.domain.model.User;
import com.forehapp.store.userModule.domain.ports.in.ChangePasswordUseCase;
import com.forehapp.store.userModule.domain.ports.in.GetUserUseCase;
import com.forehapp.store.userModule.domain.ports.in.SearchUserUseCase;
import com.forehapp.store.userModule.domain.ports.in.UpdatePhoneUseCase;
import com.forehapp.store.userModule.domain.ports.in.UpdateUserUseCase;
import com.forehapp.store.userModule.domain.ports.out.IStoreProfileDao;
import com.forehapp.store.userModule.domain.ports.out.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserUseCasesImpl implements GetUserUseCase, UpdateUserUseCase, UpdatePhoneUseCase, ChangePasswordUseCase, SearchUserUseCase {

    private final UserRepository userRepository;
    private final IStoreProfileDao storeProfileDao;
    private final PasswordEncoder passwordEncoder;

    public UserUseCasesImpl(UserRepository userRepository, IStoreProfileDao storeProfileDao, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.storeProfileDao = storeProfileDao;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserResponse getProfile(Long userId) {
        User user = findOrThrow(userId);
        String phone = storeProfileDao.findByUserId(userId).map(p -> p.getPhone()).orElse(null);
        return new UserResponse(user, phone);
    }

    @Override
    @Transactional
    public UserResponse updateProfile(Long userId, UpdateUserRequestDto dto) {
        User user = findOrThrow(userId);
        user.setName(dto.getName().trim());
        user.setLastname(dto.getLastname().trim());
        userRepository.save(user);
        String phone = storeProfileDao.findByUserId(userId).map(p -> p.getPhone()).orElse(null);
        return new UserResponse(user, phone);
    }

    @Override
    @Transactional
    public void changePassword(Long userId, ChangePasswordDto dto) {
        User user = findOrThrow(userId);

        if (!passwordEncoder.matches(dto.getCurrentPassword(), user.getPassword())) {
            throw new BadRequestException(ErrorCode.USER_PASSWORD_INCORRECT, "La contraseña actual es incorrecta");
        }
        if (passwordEncoder.matches(dto.getNewPassword(), user.getPassword())) {
            throw new BadRequestException(ErrorCode.USER_PASSWORD_SAME, "La nueva contraseña debe ser diferente a la actual");
        }

        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        userRepository.save(user);
    }

    @Override
    @Transactional
    public UserResponse updatePhone(Long userId, UpdatePhoneRequestDto dto) {
        User user = findOrThrow(userId);
        StoreProfile profile = storeProfileDao.findByUserId(userId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.USER_PROFILE_NOT_FOUND, "Store profile not found"));
        profile.setPhone(dto.getPhone().trim());
        storeProfileDao.save(profile);
        return new UserResponse(user, profile.getPhone());
    }

    @Override
    public UserSearchResponse searchByEmail(String email) {
        return userRepository.findByEmailWithActiveStoreProfile(email)
                .map(UserSearchResponse::new)
                .orElseThrow(() -> new NotFoundException(ErrorCode.USER_PROFILE_NOT_FOUND, "Usuario no encontrado"));
    }

    private User findOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.USER_PROFILE_NOT_FOUND, "Usuario no encontrado"));
    }
}
