package com.forehapp.store.returnModule.domain.ports.in;

import com.forehapp.store.returnModule.application.dto.ApproveReturnRequestDto;
import com.forehapp.store.returnModule.application.dto.RejectReturnRequestDto;
import com.forehapp.store.returnModule.application.dto.ReturnResponse;
import org.springframework.data.domain.Page;

public interface IReturnModuleService {
    Page<ReturnResponse> getPendingReturns(Long userId, int page, int size);
    ReturnResponse approveReturn(Long userId, Long returnId, ApproveReturnRequestDto dto);
    ReturnResponse rejectReturn(Long userId, Long returnId, RejectReturnRequestDto dto);
    ReturnResponse markRefunded(Long userId, Long returnId);
}
