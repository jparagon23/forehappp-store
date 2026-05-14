package com.forehapp.store.returnModule.domain.ports.out;

import com.forehapp.store.returnModule.domain.model.ReturnRequest;
import com.forehapp.store.returnModule.domain.model.ReturnStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface IReturnDao {
    Optional<ReturnRequest> findById(Long id);
    Optional<ReturnRequest> findByGroupId(Long groupId);
    List<ReturnRequest> findByBuyerId(Long buyerId);
    Page<ReturnRequest> findByStatus(ReturnStatus status, Pageable pageable);
    ReturnRequest save(ReturnRequest returnRequest);
}
