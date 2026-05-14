package com.forehapp.store.returnModule.domain.ports.in;

import com.forehapp.store.returnModule.application.dto.CreateReturnRequestDto;
import com.forehapp.store.returnModule.application.dto.ReturnResponse;

import java.util.List;

public interface IReturnService {
    ReturnResponse createReturn(Long userId, CreateReturnRequestDto dto);
    List<ReturnResponse> getMyReturns(Long userId);
    ReturnResponse getReturn(Long userId, Long returnId);
}
