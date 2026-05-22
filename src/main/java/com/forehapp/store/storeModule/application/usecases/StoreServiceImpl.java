package com.forehapp.store.storeModule.application.usecases;

import com.forehapp.store.general.exceptions.BadRequestException;
import com.forehapp.store.general.exceptions.ErrorCode;
import com.forehapp.store.general.exceptions.ForbiddenException;
import com.forehapp.store.general.exceptions.NotFoundException;
import com.forehapp.store.general.utils.SlugUtils;
import com.forehapp.store.storeModule.application.dto.*;
import com.forehapp.store.storeModule.domain.model.Store;
import com.forehapp.store.storeModule.domain.model.StoreMembership;
import com.forehapp.store.storeModule.domain.model.StoreMemberRole;
import com.forehapp.store.storeModule.domain.ports.in.IStoreService;
import com.forehapp.store.storeModule.domain.ports.out.IStoreDao;
import com.forehapp.store.storeModule.domain.ports.out.IStoreMembershipDao;
import com.forehapp.store.userModule.domain.model.StoreProfile;
import com.forehapp.store.userModule.domain.model.StoreRole;
import com.forehapp.store.userModule.domain.ports.out.IStoreProfileDao;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class StoreServiceImpl implements IStoreService {

    private final IStoreDao storeDao;
    private final IStoreMembershipDao membershipDao;
    private final IStoreProfileDao storeProfileDao;

    public StoreServiceImpl(IStoreDao storeDao,
                            IStoreMembershipDao membershipDao,
                            IStoreProfileDao storeProfileDao) {
        this.storeDao = storeDao;
        this.membershipDao = membershipDao;
        this.storeProfileDao = storeProfileDao;
    }

    @Override
    @Transactional
    public StoreResponse createStore(CreateStoreRequestDto dto, Long userId) {
        StoreProfile adminProfile = resolveProfile(userId);
        if (!adminProfile.getRoles().contains(StoreRole.STORE_ADMIN)) {
            throw new ForbiddenException(ErrorCode.STORE_ADMIN_REQUIRED, "Only platform admins can create stores");
        }

        StoreProfile ownerProfile = storeProfileDao.findByUserId(dto.getOwnerId())
                .orElseThrow(() -> new NotFoundException(ErrorCode.USER_PROFILE_NOT_FOUND, "Owner user not found"));

        String slug = generateUniqueSlug(dto.getName());

        Store store = new Store();
        store.setName(dto.getName().trim());
        store.setSlug(slug);
        store.setDescription(dto.getDescription());
        store = storeDao.save(store);

        StoreMembership ownership = new StoreMembership();
        ownership.setStore(store);
        ownership.setStoreProfile(ownerProfile);
        ownership.setRole(StoreMemberRole.OWNER);
        membershipDao.save(ownership);

        if (!ownerProfile.getRoles().contains(StoreRole.SELLER)) {
            ownerProfile.getRoles().add(StoreRole.SELLER);
            storeProfileDao.save(ownerProfile);
        }

        return new StoreResponse(store);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MyStoreResponse> getMyStores(Long userId) {
        return storeProfileDao.findByUserId(userId)
                .map(profile -> membershipDao.findActiveByStoreProfileId(profile.getId()).stream()
                        .map(MyStoreResponse::new)
                        .toList())
                .orElse(List.of());
    }

    @Override
    @Transactional(readOnly = true)
    public StoreResponse getStore(Long storeId, Long userId) {
        StoreProfile profile = resolveProfile(userId);
        StoreMembership membership = resolveActiveMembership(storeId, profile.getId());
        return new StoreResponse(membership.getStore());
    }

    @Override
    @Transactional
    public StoreResponse updateStore(Long storeId, UpdateStoreRequestDto dto, Long userId) {
        StoreProfile profile = resolveProfile(userId);
        StoreMembership membership = resolveActiveMembership(storeId, profile.getId());
        requireRole(membership, StoreMemberRole.OWNER, StoreMemberRole.MANAGER);

        Store store = membership.getStore();
        if (dto.getName() != null) store.setName(dto.getName().trim());
        if (dto.getDescription() != null) store.setDescription(dto.getDescription());
        if (dto.getFreeShippingMinAmount() != null) store.setFreeShippingMinAmount(dto.getFreeShippingMinAmount());

        return new StoreResponse(storeDao.save(store));
    }

    @Override
    @Transactional
    public StoreMembershipResponse inviteMember(Long storeId, InviteMemberRequestDto dto, Long userId) {
        StoreProfile inviterProfile = resolveProfile(userId);
        StoreMembership inviterMembership = resolveActiveMembership(storeId, inviterProfile.getId());
        requireRole(inviterMembership, StoreMemberRole.OWNER, StoreMemberRole.MANAGER);

        if (inviterMembership.getRole() == StoreMemberRole.MANAGER
                && dto.getRole() != StoreMemberRole.STAFF) {
            throw new ForbiddenException(ErrorCode.STORE_MANAGER_REMOVE_RESTRICTED, "Managers can only invite staff members");
        }

        StoreProfile targetProfile = storeProfileDao.findByUserId(dto.getUserId())
                .orElseThrow(() -> new NotFoundException(ErrorCode.USER_PROFILE_NOT_FOUND, "User not found"));

        if (targetProfile.getId().equals(inviterProfile.getId())) {
            throw new BadRequestException(ErrorCode.STORE_ALREADY_MEMBER, "You are already a member of this store");
        }

        membershipDao.findActiveByStoreIdAndStoreProfileId(storeId, targetProfile.getId())
                .ifPresent(m -> {
                    throw new BadRequestException(ErrorCode.STORE_ALREADY_MEMBER, "User is already a member of this store");
                });

        StoreMembership newMembership = new StoreMembership();
        newMembership.setStore(inviterMembership.getStore());
        newMembership.setStoreProfile(targetProfile);
        newMembership.setRole(dto.getRole());
        StoreMembership saved = membershipDao.save(newMembership);

        if (!targetProfile.getRoles().contains(StoreRole.SELLER)) {
            targetProfile.getRoles().add(StoreRole.SELLER);
            storeProfileDao.save(targetProfile);
        }

        return new StoreMembershipResponse(saved);
    }

    @Override
    @Transactional
    public StoreMembershipResponse updateMemberRole(Long storeId, Long membershipId,
                                                    UpdateMemberRoleRequestDto dto, Long userId) {
        StoreProfile callerProfile = resolveProfile(userId);
        StoreMembership callerMembership = resolveActiveMembership(storeId, callerProfile.getId());
        requireRole(callerMembership, StoreMemberRole.OWNER);

        StoreMembership target = membershipDao.findByIdAndStoreId(membershipId, storeId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.USER_PROFILE_NOT_FOUND, "Membership not found"));

        if (target.getId().equals(callerMembership.getId())) {
            throw new BadRequestException(ErrorCode.STORE_ROLE_SELF_CHANGE, "Cannot change your own role");
        }

        if (target.getRole() == StoreMemberRole.OWNER && dto.getRole() != StoreMemberRole.OWNER) {
            long ownerCount = membershipDao.countActiveOwnersByStoreId(storeId);
            if (ownerCount <= 1) {
                throw new BadRequestException(ErrorCode.STORE_LAST_OWNER, "Cannot demote the last owner");
            }
        }

        target.setRole(dto.getRole());
        return new StoreMembershipResponse(membershipDao.save(target));
    }

    @Override
    @Transactional
    public void removeMember(Long storeId, Long membershipId, Long userId) {
        StoreProfile callerProfile = resolveProfile(userId);
        StoreMembership callerMembership = resolveActiveMembership(storeId, callerProfile.getId());

        StoreMembership target = membershipDao.findByIdAndStoreId(membershipId, storeId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.USER_PROFILE_NOT_FOUND, "Membership not found"));

        boolean isSelf = target.getId().equals(callerMembership.getId());

        if (isSelf) {
            if (target.getRole() == StoreMemberRole.OWNER
                    && membershipDao.countActiveOwnersByStoreId(storeId) <= 1) {
                throw new BadRequestException(ErrorCode.STORE_LAST_OWNER_LEAVE, "Cannot leave as the last owner. Transfer ownership first.");
            }
        } else {
            if (callerMembership.getRole() == StoreMemberRole.STAFF) {
                throw new ForbiddenException(ErrorCode.STORE_STAFF_CANNOT_REMOVE, "Staff members cannot remove other members");
            }
            if (callerMembership.getRole() == StoreMemberRole.MANAGER
                    && target.getRole() != StoreMemberRole.STAFF) {
                throw new ForbiddenException(ErrorCode.STORE_MANAGER_REMOVE_RESTRICTED, "Managers can only remove staff members");
            }
        }

        target.setActive(false);
        membershipDao.save(target);
    }

    @Override
    @Transactional(readOnly = true)
    public List<StoreMembershipResponse> getMembers(Long storeId, Long userId) {
        StoreProfile profile = resolveProfile(userId);
        resolveActiveMembership(storeId, profile.getId());
        return membershipDao.findActiveByStoreId(storeId).stream()
                .map(StoreMembershipResponse::new)
                .toList();
    }

    private StoreProfile resolveProfile(Long userId) {
        return storeProfileDao.findByUserId(userId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.USER_PROFILE_NOT_FOUND, "Store profile not found"));
    }

    private StoreMembership resolveActiveMembership(Long storeId, Long storeProfileId) {
        return membershipDao.findActiveByStoreIdAndStoreProfileId(storeId, storeProfileId)
                .orElseThrow(() -> new ForbiddenException(ErrorCode.STORE_ACCESS_DENIED, "You are not a member of this store"));
    }

    private void requireRole(StoreMembership membership, StoreMemberRole... allowedRoles) {
        for (StoreMemberRole role : allowedRoles) {
            if (membership.getRole() == role) return;
        }
        throw new ForbiddenException(ErrorCode.STORE_INSUFFICIENT_PERMISSIONS, "Insufficient permissions for this action");
    }

    private String generateUniqueSlug(String name) {
        String base = SlugUtils.slugify(name);
        String slug = base;
        int attempt = 2;
        while (storeDao.existsBySlug(slug)) {
            slug = base + "-" + attempt++;
        }
        return slug;
    }
}
