package com.forehapp.store.userModule.application.usecases;

import com.forehapp.store.general.exceptions.ErrorCode;
import com.forehapp.store.general.exceptions.ForbiddenException;
import com.forehapp.store.general.exceptions.NotFoundException;
import com.forehapp.store.userModule.application.dto.UserStatsResponse;
import com.forehapp.store.userModule.domain.model.StoreProfile;
import com.forehapp.store.userModule.domain.model.StoreRole;
import com.forehapp.store.userModule.domain.ports.in.IAdminUserService;
import com.forehapp.store.userModule.domain.ports.out.IStoreProfileDao;
import com.forehapp.store.userModule.domain.ports.out.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class AdminUserServiceImpl implements IAdminUserService {

    private static final int RECENT_USERS_LIMIT = 10;
    private static final int TREND_DAYS = 30;

    private final UserRepository userRepository;
    private final IStoreProfileDao storeProfileDao;

    public AdminUserServiceImpl(UserRepository userRepository, IStoreProfileDao storeProfileDao) {
        this.userRepository = userRepository;
        this.storeProfileDao = storeProfileDao;
    }

    @Override
    @Transactional(readOnly = true)
    public UserStatsResponse getUserStats(Long adminUserId) {
        requireAdmin(adminUserId);
        return new UserStatsResponse(
                userRepository.countAll(),
                userRepository.findRecentRegistrations(RECENT_USERS_LIMIT),
                userRepository.findRegistrationTrend(LocalDateTime.now().minusDays(TREND_DAYS))
        );
    }

    private void requireAdmin(Long userId) {
        StoreProfile profile = storeProfileDao.findByUserId(userId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.USER_PROFILE_NOT_FOUND, "Store profile not found"));
        if (!profile.getRoles().contains(StoreRole.STORE_ADMIN)) {
            throw new ForbiddenException(ErrorCode.STORE_ADMIN_REQUIRED, "Admin access required");
        }
    }
}
