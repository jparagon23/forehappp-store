package com.forehapp.store.returnModule.application.usecases;

import com.forehapp.store.returnModule.application.dto.ApproveReturnRequestDto;
import com.forehapp.store.returnModule.application.dto.RejectReturnRequestDto;
import com.forehapp.store.returnModule.application.dto.ReturnResponse;
import com.forehapp.store.returnModule.domain.model.ReturnRequest;
import com.forehapp.store.returnModule.domain.model.ReturnStatus;
import com.forehapp.store.returnModule.domain.ports.in.IReturnModuleService;
import com.forehapp.store.returnModule.domain.ports.out.IReturnDao;
import com.forehapp.store.userModule.domain.model.StoreProfile;
import com.forehapp.store.userModule.domain.model.StoreRole;
import com.forehapp.store.userModule.domain.ports.out.IStoreProfileDao;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ReturnModuleServiceImpl implements IReturnModuleService {

    private final IReturnDao returnDao;
    private final IStoreProfileDao storeProfileDao;

    public ReturnModuleServiceImpl(IReturnDao returnDao, IStoreProfileDao storeProfileDao) {
        this.returnDao = returnDao;
        this.storeProfileDao = storeProfileDao;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReturnResponse> getPendingReturns(Long userId, int page, int size) {
        requireAdmin(userId);
        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "createdAt"));
        return returnDao.findByStatus(ReturnStatus.PENDIENTE, pageable).map(ReturnServiceImpl::toResponse);
    }

    @Override
    @Transactional
    public ReturnResponse approveReturn(Long userId, Long returnId, ApproveReturnRequestDto dto) {
        requireAdmin(userId);
        ReturnRequest returnRequest = findPendingReturn(returnId);
        returnRequest.setStatus(ReturnStatus.APROBADA);
        returnRequest.setRefundAmount(dto.refundAmount());
        returnRequest.setAdminNotes(dto.adminNotes());
        return ReturnServiceImpl.toResponse(returnDao.save(returnRequest));
    }

    @Override
    @Transactional
    public ReturnResponse rejectReturn(Long userId, Long returnId, RejectReturnRequestDto dto) {
        requireAdmin(userId);
        ReturnRequest returnRequest = findPendingReturn(returnId);
        returnRequest.setStatus(ReturnStatus.RECHAZADA);
        returnRequest.setAdminNotes(dto.adminNotes());
        return ReturnServiceImpl.toResponse(returnDao.save(returnRequest));
    }

    @Override
    @Transactional
    public ReturnResponse markRefunded(Long userId, Long returnId) {
        requireAdmin(userId);
        ReturnRequest returnRequest = returnDao.findById(returnId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Return request not found"));
        if (returnRequest.getStatus() != ReturnStatus.APROBADA) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only approved returns can be marked as refunded");
        }
        returnRequest.setStatus(ReturnStatus.REEMBOLSADA);
        return ReturnServiceImpl.toResponse(returnDao.save(returnRequest));
    }

    private ReturnRequest findPendingReturn(Long returnId) {
        ReturnRequest returnRequest = returnDao.findById(returnId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Return request not found"));
        if (returnRequest.getStatus() != ReturnStatus.PENDIENTE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only pending returns can be processed");
        }
        return returnRequest;
    }

    private void requireAdmin(Long userId) {
        StoreProfile profile = storeProfileDao.findByUserId(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Store profile not found"));
        if (!profile.getRoles().contains(StoreRole.STORE_ADMIN)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Admin access required");
        }
    }
}
