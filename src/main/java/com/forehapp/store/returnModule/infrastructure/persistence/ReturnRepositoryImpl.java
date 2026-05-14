package com.forehapp.store.returnModule.infrastructure.persistence;

import com.forehapp.store.returnModule.domain.model.ReturnRequest;
import com.forehapp.store.returnModule.domain.model.ReturnStatus;
import com.forehapp.store.returnModule.domain.ports.out.IReturnDao;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class ReturnRepositoryImpl implements IReturnDao {

    private final IReturnRepository repository;

    public ReturnRepositoryImpl(IReturnRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<ReturnRequest> findById(Long id) {
        return repository.findByIdWithDetails(id);
    }

    @Override
    public Optional<ReturnRequest> findByGroupId(Long groupId) {
        return repository.findByGroupId(groupId);
    }

    @Override
    public List<ReturnRequest> findByBuyerId(Long buyerId) {
        return repository.findByBuyerId(buyerId);
    }

    @Override
    public Page<ReturnRequest> findByStatus(ReturnStatus status, Pageable pageable) {
        return repository.findByStatus(status, pageable);
    }

    @Override
    public ReturnRequest save(ReturnRequest returnRequest) {
        return repository.save(returnRequest);
    }
}
