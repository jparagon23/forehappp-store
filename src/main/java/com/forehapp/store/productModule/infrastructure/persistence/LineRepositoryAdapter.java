package com.forehapp.store.productModule.infrastructure.persistence;

import com.forehapp.store.productModule.domain.model.Line;
import com.forehapp.store.productModule.domain.ports.out.ILineDao;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class LineRepositoryAdapter implements ILineDao {

    private final ILineJpaRepository jpaRepository;

    public LineRepositoryAdapter(ILineJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Optional<Line> findById(Long id) {
        return jpaRepository.findByIdWithBrand(id);
    }

    @Override
    public List<Line> findAllByBrandId(Long brandId) {
        return jpaRepository.findByBrandId(brandId);
    }

    @Override
    public Line save(Line line) {
        return jpaRepository.save(line);
    }
}
