package com.forehapp.store.userModule.application.usecases;

import com.forehapp.store.general.exceptions.ErrorCode;
import com.forehapp.store.general.exceptions.ForbiddenException;
import com.forehapp.store.general.exceptions.NotFoundException;
import com.forehapp.store.orderModule.domain.ports.out.IOrderDao;
import com.forehapp.store.userModule.application.dto.UserStatsResponse;
import com.forehapp.store.userModule.domain.model.StoreProfile;
import com.forehapp.store.userModule.domain.model.StoreRole;
import com.forehapp.store.userModule.domain.model.UserSearchResult;
import com.forehapp.store.userModule.domain.ports.in.IAdminUserService;
import com.forehapp.store.userModule.domain.ports.out.IStoreProfileDao;
import com.forehapp.store.userModule.domain.ports.out.UserRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

@Service
public class AdminUserServiceImpl implements IAdminUserService {

    private static final int RECENT_USERS_LIMIT = 10;
    private static final int TREND_DAYS = 30;
    private static final int SEARCH_LIMIT = 20;
    private static final int SEARCH_MIN_LENGTH = 2;
    private static final int SEARCH_MAX_TERMS = 5;

    private final UserRepository userRepository;
    private final IOrderDao orderDao;
    private final IStoreProfileDao storeProfileDao;

    public AdminUserServiceImpl(UserRepository userRepository, IOrderDao orderDao, IStoreProfileDao storeProfileDao) {
        this.userRepository = userRepository;
        this.orderDao = orderDao;
        this.storeProfileDao = storeProfileDao;
    }

    @Override
    @Cacheable(value = "admin-user-stats", key = "'stats'")
    @Transactional(readOnly = true)
    public UserStatsResponse getUserStats(Long adminUserId) {
        requireAdmin(adminUserId);

        long total = userRepository.countAll();
        long usersWhoOrdered = orderDao.countDistinctBuyers();
        double conversionRate = total == 0 ? 0.0
                : Math.round((usersWhoOrdered * 1000.0 / total)) / 10.0;

        return new UserStatsResponse(
                total,
                userRepository.countNewSince(LocalDate.now().atStartOfDay()),
                userRepository.countNewSince(LocalDate.now().minusDays(7).atStartOfDay()),
                userRepository.countNewSince(LocalDate.now().withDayOfMonth(1).atStartOfDay()),
                usersWhoOrdered,
                conversionRate,
                userRepository.countWithPhone(),
                userRepository.countActive(),
                userRepository.findRecentRegistrations(RECENT_USERS_LIMIT),
                userRepository.findRegistrationTrend(LocalDateTime.now().minusDays(TREND_DAYS))
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserSearchResult> searchUsers(Long adminUserId, String query) {
        requireAdmin(adminUserId);

        String normalized = query == null ? "" : query.trim().toLowerCase(Locale.ROOT);
        if (normalized.length() < SEARCH_MIN_LENGTH) {
            return List.of();
        }
        List<String> terms = Arrays.stream(normalized.split("\\s+"))
                .filter(t -> !t.isBlank())
                .limit(SEARCH_MAX_TERMS)
                .toList();
        return userRepository.search(terms, SEARCH_LIMIT);
    }

    private void requireAdmin(Long userId) {
        StoreProfile profile = storeProfileDao.findByUserId(userId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.USER_PROFILE_NOT_FOUND, "Store profile not found"));
        if (!profile.getRoles().contains(StoreRole.STORE_ADMIN)) {
            throw new ForbiddenException(ErrorCode.STORE_ADMIN_REQUIRED, "Admin access required");
        }
    }
}
