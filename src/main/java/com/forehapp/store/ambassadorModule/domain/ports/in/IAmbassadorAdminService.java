package com.forehapp.store.ambassadorModule.domain.ports.in;

import com.forehapp.store.ambassadorModule.application.dto.AmbassadorResponse;
import com.forehapp.store.ambassadorModule.application.dto.CommissionResponse;
import com.forehapp.store.ambassadorModule.application.dto.CreateAmbassadorRequestDto;
import com.forehapp.store.ambassadorModule.application.dto.UpdateAmbassadorRequestDto;

import java.util.List;

public interface IAmbassadorAdminService {
    AmbassadorResponse create(Long adminUserId, CreateAmbassadorRequestDto dto);
    AmbassadorResponse update(Long adminUserId, Long ambassadorId, UpdateAmbassadorRequestDto dto);
    List<AmbassadorResponse> findAll(Long adminUserId);
    AmbassadorResponse findById(Long adminUserId, Long ambassadorId);
    List<CommissionResponse> findCommissionsByAmbassador(Long adminUserId, Long ambassadorId);
    CommissionResponse payCommission(Long adminUserId, Long commissionId);
}
